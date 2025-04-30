/*
 *
 *  * Software Name : Compose
 *  * SPDX-FileCopyrightText: Copyright (c) Orange SA
 *  * SPDX-License-Identifier:  MIT
 *  *
 *  * This software is distributed under the MIT License,
 *  * see the "LICENSE.txt" file for more details or https://spdx.org/licenses/MIT.html
 *  *
 *  * <Authors: optional: authors list / see CONTRIBUTORS>
 *
 */

package smart_contracts.controllers


import better.files.File
import common.utils.TraceUtils
import org.web3j.crypto.Credentials
import play.api.Configuration
import play.api.libs.Files.TemporaryFile
import play.api.libs.json._
import smart_contracts.dao.{ContractDBDAO, LogDao}
import smart_contracts.model.dao.{Contract, ContractOptions, LogProject}
import smart_contracts.model.{ContractInstructions, ContractParameters}

import java.nio.file.Paths
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId, ZonedDateTime}
import java.util.UUID.randomUUID
import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import scala.sys.process.{Process, ProcessLogger}
import scala.util.matching.Regex

class ContractsManager @Inject()(
                                  configuration: Configuration,
                                  contractRepo: ContractDBDAO,
                                  logDao: LogDao,
                                ) extends TraceUtils {

  private val projectDirectory: File = File(s"${configuration.get[String]("project.repository")}")

  def initProjectsDirectories(): Any = {
    if (projectDirectory.notExists) {
      projectDirectory.createDirectories()
    }
  }

  private val isWindows = System.getProperty("os.name").toLowerCase().contains("windows")
  private val hardhat: String = "npx hardhat"
  private val npm: String = "npm"
  val confirmDeployment: String = if (isWindows) "$env:HARDHAT_IGNITION_CONFIRM_DEPLOYMENT='false'; " else "export HARDHAT_IGNITION_CONFIRM_DEPLOYMENT=false &&"

  def process(command: String, repo: File, projectId: String, method: String): Boolean = {
    entering("process")
    val logger = ProcessLogger(line => {
      debug(s"$command", line)
      logDao.create(LogProject(randomUUID.toString, projectId, method, Instant.now().getEpochSecond.toString, line))
    })
    val fullCommand = if (isWindows) Seq("powershell", "-Command", command) else Seq("/bin/sh", "-c", command)
    Process(fullCommand, repo.toJava).!(logger) == 0
  }

  // INFO: Use "HARDHAT_IGNITION_CONFIRM_DEPLOYMENT=false" to avoid confirmation request
  def migrate(path: String, projectId: String, contractOptions: ContractOptions, blockchain: String, p: (String, File, String, String) => Boolean = process)(credentials: Credentials): Boolean = {
    entering("migrate")
    generateConfig(path, contractOptions, blockchain)(credentials)
    val repo = File(path)
    debug("test",s"$confirmDeployment $hardhat ignition deploy ./ignition/modules/deploy.js --network $blockchain " + repo.toString() +projectId )

    p(s"$confirmDeployment $hardhat ignition deploy ./ignition/modules/deploy.js --network $blockchain", repo, projectId, "migrate")
  }

  def proxyConfiguration(path: String, projectId: String, options: ContractOptions, p: (String, File, String, String) => Boolean = process): Boolean = {
    entering("proxyConfiguration")
    val repo = File(path)

    val proxySettings = List(
      options.httpProxy.map(proxy => s"npm config set proxy $proxy"),
      options.httpsProxy.map(proxy => s"npm config set https-proxy $proxy")
    ).flatten

    proxySettings.exists(command => p(command, repo, projectId, "proxyConfiguration"))
  }

  def installNpmDependencies(path: String, projectId: String, p: (String, File, String, String) => Boolean = process): Boolean = {
    entering("installNpmDependencies")
    val repo = File(path)
    p(s"$npm install --save-dev hardhat@2.22.6", repo, projectId, "installNpmDependencies")
    p(s"$npm install --save-dev @nomicfoundation/hardhat-toolbox@5.0.0", repo, projectId, "installNpmDependencies")
    p(s"$npm install undici", repo, projectId, "installNpmDependencies")
  }

  def generateConfig(path: String, options: ContractOptions, blockchain: String)(credentials: Credentials): Unit = {
    entering("generateConfig")

    val baseConfig =
      """|require("@nomicfoundation/hardhat-toolbox");
         |""".stripMargin

    val proxyConfig = (options.httpProxy orElse options.httpsProxy).map { proxy =>
      s"""|
          |const { ProxyAgent, setGlobalDispatcher } = require("undici");
          |const proxyAgent = new ProxyAgent('$proxy');
          |setGlobalDispatcher(proxyAgent);
          |""".stripMargin
    }.getOrElse("")

    val optimizerConfig = options.optimizerRuns.map { runs =>
      s"""|      optimizer: {
          |        enabled: true,
          |        runs: $runs,
          |      },
          |""".stripMargin
    }.getOrElse("")

    val evmVersionConfig = options.evmVersion.map { evmVersion =>
      s"""|      evmVersion: "$evmVersion"""".stripMargin
    }.getOrElse("")

    val settingsConfig = if (optimizerConfig.nonEmpty || evmVersionConfig.nonEmpty) {
      s"""|    settings: {
          |$optimizerConfig$evmVersionConfig
          |    }""".stripMargin
    } else ""

    debug("eckeypai ", credentials.getEcKeyPair.getPrivateKey.toString(16))

    val networkConfig =
      s"""|
          |module.exports = {
          |  networks: {
          |    $blockchain: {
          |      url: "${configuration.get[String](s"blockchain.$blockchain.url")}",
          |      accounts: ["${credentials.getEcKeyPair.getPrivateKey.toString(16)}"]
          |    },
          |  },
          |  mocha: {
          |    // timeout: 100000
          |  },
          |  solidity: {
          |    version: "${options.version}",
          |$settingsConfig
          |  }
          |};""".stripMargin

    val fileContent = baseConfig + proxyConfig + networkConfig

    val configFile = File(path + "/hardhat.config.js")

    if (configFile.exists) configFile.delete()
    configFile.createFile()
    debug("generateConfig", "create file " + configFile)
    configFile.appendText(fileContent)
  }

  def uploadFile(file: TemporaryFile, body: JsValue, projectId: String, userId: String, blockchain: String)(credentials: Credentials): Future[ListBuffer[Contract]] = {
    entering("uploadFile")
    initProjectsDirectories()
    (body \ "options").validate[ContractOptions].fold(
      _ => Future.failed(new Exception("Invalid options")),
      contractOptions => {
        val deploymentFile: File = File(s"$projectDirectory/$projectId/ignition/modules/deploy.js")
        debug("uploadFile","create file " + deploymentFile.pathAsString)
        file.copyTo(Paths.get(s"$projectDirectory/$projectId.zip"), replace = true)
        debug("uploadFile",s"copy file $projectDirectory/$projectId.zip")
        createProject(s"$projectDirectory/$projectId")
        
        createHeader(deploymentFile)
        createDeploy(s"$projectDirectory/$projectId", body, deploymentFile)

        proxyConfiguration(s"$projectDirectory/$projectId", projectId, contractOptions)
        installNpmDependencies(s"$projectDirectory/$projectId", projectId)
        migrate(s"$projectDirectory/$projectId", projectId, contractOptions, blockchain)(credentials)
        val contracts = createContracts(s"$projectDirectory/$projectId", blockchain, projectId, userId)

        val fileToDelete = File(s"$projectDirectory/$projectId.zip")
        if (fileToDelete.exists) fileToDelete.delete()
        Future.successful(contracts)
      }
    )
  }

  def createProject(filePath: String): Unit = {
    entering("createProject")
    val newProjectDirectory: File = File(filePath)
    debug("createProject", "Step 1")
    val zipDirectory = File(s"$filePath.zip")
    debug("createProject", "Step 2")
    val contractDirectory = File(s"$newProjectDirectory/contracts")
    debug("createProject", "Step 3")
    newProjectDirectory.createDirectory()
    debug("createProject", "Step 4")
    val ignitionDirectory = File(s"$newProjectDirectory/ignition")
    debug("createProject", "Step 5")
    ignitionDirectory.createDirectory()
    debug("createProject", "Step 6")
    val scriptDirectory = File(s"$ignitionDirectory/modules")
    debug("createProject", "Step 7")
    scriptDirectory.createDirectory()
    debug("createProject", "Step 8")
    zipDirectory.unzipTo(contractDirectory)
    debug("createProject", "Step 9")
    zipDirectory.delete()
    debug("createProject", "Finished")
  }

  private def isAuxiliary(path: String, name: String, pattern: Regex): Boolean = {
    val fullPath = File(path, "/contracts/" + name + ".sol")
    val file = Source.fromFile(fullPath.pathAsString)
    val content = file.mkString
    file.close()
    pattern.findFirstMatchIn(content) match {
      case Some(m) if m.group(1) == name => true
      case _ => false
    }
  }

  private def isContract(path: String, name: String): Boolean = {
    val contractPattern: Regex = """contract\s+(\w+).*\{""".r
    isAuxiliary(path, name, contractPattern) && !isLibrary(path, name)
  }

  private def isLibrary(path: String, name: String): Boolean = {
    val libraryPattern: Regex = """library\s+(\w+).*\{""".r
    isAuxiliary(path, name, libraryPattern) && !isContract(path, name)
  }

  private def appendInfo(contract: ContractInstructions, path: String, deploymentFile: File): Unit = {
    entering("appendInfo")
    val name = contract.name
    val links = contract.links
    val libraries = links.filter(isLibrary(path, _)).mkString(", ")
    val after = links.filterNot(isLibrary(path, _)).mkString(", ")

    deploymentFile.append(s"""|  const $name = m.${if (isLibrary(path, name)) "library" else "contract"}("$name"""".stripMargin)

    if (isContract(path, name)) {
      val parameters = contract.parameters.getOrElse(List.empty[ContractParameters])
      val parametersToAdd = parameters.map { param =>
        param.valueType match {
          case "bool" => param.value.rawValue.toString
          case "int" | "int8" | "int16" | "int32" | "int64" | "int128" | "int256" => param.value.rawValue.toString
          case "uint" | "uint8" | "uint16" | "uint32" | "uint64" | "uint128" | "uint256" => param.value.rawValue.toString
          case "address" => s""""${param.value.rawValue}""""
          case "bytes" | "bytes1" | "bytes2" | "bytes3" | "bytes4" | "bytes5" | "bytes6" | "bytes7" | "bytes8" | "bytes9" | "bytes10" | "bytes11" | "bytes12" | "bytes13" | "bytes14" | "bytes15" | "bytes16" | "bytes17" | "bytes18" | "bytes19" | "bytes20" | "bytes21" | "bytes22" | "bytes23" | "bytes24" | "bytes25" | "bytes26" | "bytes27" | "bytes28" | "bytes29" | "bytes30" | "bytes31" | "bytes32" => s""""${param.value.rawValue}""""
          case "string" => s""""${param.value.rawValue}""""
          case _ => throw new IllegalArgumentException(s"Unsupported parameter type: ${param.valueType}")
        }
      }.mkString(", ")
      deploymentFile.append(s""", [$parametersToAdd]""".stripMargin)
    }

    if (libraries.nonEmpty || after.nonEmpty) {
      deploymentFile.appendLine(s""", {""".stripMargin)
      if (libraries.nonEmpty) deploymentFile.appendLine(s"""|    libraries: { $libraries },""".stripMargin)
      if (after.nonEmpty) deploymentFile.appendLine(s"""|    after: [ $after ]""".stripMargin)
      deploymentFile.append(s"""|  }""".stripMargin)
    }

    deploymentFile.appendLine(s""");\n""".stripMargin)
  }

  def createDeploy(path: String, body: JsValue, deploymentFile: File): Unit = {
    entering("createDeploy")

    (body \ "migration").validate[List[ContractInstructions]].fold(
      _ => Future.failed(new Exception("Invalid options")),
      migration => {
        val contractsList = new ListBuffer[String]()

        migration.foreach { contract =>
          if (isLibrary(path, contract.name) || isContract(path, contract.name)) {
            if (!contractsList.contains(contract.name)) contractsList += contract.name
            appendInfo(contract, path, deploymentFile)
          }
        }
        deploymentFile.append(s"""|  return { """.stripMargin)
        contractsList.foreach(contract => deploymentFile.append(s"""$contract, """.stripMargin))
        deploymentFile.appendLine(s"""};""".stripMargin)
        deploymentFile.appendLine(s"""|});""".stripMargin)
      }
    )
  }

  def createHeader(deploymentFile: File): Unit = {
    entering("createHeader")
    deploymentFile.createFile()
    deploymentFile.appendLine(
      s"""|const { buildModule } = require("@nomicfoundation/hardhat-ignition/modules");
          |
          |module.exports = buildModule("DeployerModule", (m) => {
          |""".stripMargin)
  }

  def createContracts(path: String, blockchain: String, project: String, owner: String): ListBuffer[Contract] = {
    entering("createContracts")
    val root = File(path)
    val deploymentsDir = root / "ignition" / "deployments"
    val repo = deploymentsDir.list.filter(file => file.isDirectory).toList.headOption
    val contractsList = new ListBuffer[Contract]
    repo match {
      case Some(dir) =>
        val chainNumber = dir.name.split("-").lastOption.getOrElse("")
        val deployedAddressesFile = dir / "deployed_addresses.json"
        val deployedAddresses = if (deployedAddressesFile.exists) {
          Json.parse(deployedAddressesFile.contentAsString).asOpt[Map[String, String]].getOrElse(Map.empty[String, String])
        } else {
          Map.empty[String, String]
        }
        val artifactsDir = dir / "artifacts"
        artifactsDir.listRecursively
          .filter(file => file.isRegularFile && file.extension.contains(".json") && !file.name.endsWith(".dbg.json"))
          .foreach { contract =>
            val contractContent = Json.parse(contract.contentAsString) match {
              case json: JsObject =>
                val contractName = (json \ "contractName").asOpt[String].getOrElse("")
                val address = deployedAddresses.getOrElse(s"DeployerModule#$contractName", "")
                val formatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                val parisZoneId: ZoneId = ZoneId.of("Europe/Paris")
                val updatedAt: String = formatter.format(ZonedDateTime.ofInstant(Instant.now(), parisZoneId))
                val enrichedJson = json ++ Json.obj(
                  "networks" -> Json.obj(
                    chainNumber -> Json.obj(
                      "address" -> JsString(address)
                    )
                  ),
                  "updatedAt" -> JsString(updatedAt)
                )
                Json.prettyPrint(enrichedJson)
              case _ => contract.contentAsString
            }
            val newContract = Contract(
              _id = None,
              name = contract.nameWithoutExtension.split("#").lastOption.getOrElse(contract.nameWithoutExtension),
              blockchain = blockchain,
              file = contractContent,
              project = project,
              owner = owner
            )
            contractsList += newContract
            createContract(newContract)
          }
      case None =>
        throw new Exception("No folders found in deployments")
    }
    
    contractsList
  }

  def createContract(contract: Contract): Future[Contract] = {
    entering("createContract")
    val newContract: Contract = Contract(
      _id = Some(randomUUID.toString),
      name = contract.name,
      blockchain = contract.blockchain,
      file = contract.file,
      project = contract.project,
      owner = contract.owner
    )
    contractRepo.create(newContract: Contract).map(_ => newContract)
  }

  def getContractsByProject(projectId: String): Future[Seq[Contract]] = {
    entering("getContractsByProject")
    contractRepo.findBy("project")(projectId)
  }

  def getContractsByUserId(userId: String): Future[Seq[Contract]] = {
    entering("getContractsByProject")
    contractRepo.findBy("owner")(userId)
  }

}

object ContractsManager {
  def apply(configuration: Configuration, contractRepo: ContractDBDAO, logDao: LogDao): ContractsManager = new ContractsManager(configuration, contractRepo, logDao)
}
