package managers

import better.files.File
import com.typesafe.config.ConfigFactory
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.web3j.crypto.{Credentials, ECKeyPair}
import play.api.ConfigLoader.stringLoader
import play.api.Configuration
import play.api.libs.Files.{SingletonTemporaryFileCreator, TemporaryFile}
import play.api.libs.json.{JsValue, Json}
import play.api.test.PlaySpecification
import smart_contracts.controllers.ContractsManager
import smart_contracts.dao.{ContractDBDAO, LogDao}
import smart_contracts.model.dao.{Contract, ContractOptions}

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class ContractsManagerTest extends PlaySpecification {
  sequential
  implicit val configuration: Configuration = new Configuration(ConfigFactory.load("application.test.conf"))
  val testDirectory: File = File(s"${configuration.get[String]("test.repository")}")
  val projectTestId: String = "projectTest"
  val projectTestDirectory: String = s"${testDirectory.pathAsString}/$projectTestId"

  val contract: Contract = new Contract(Some("_id"), "contract.sol", "ethereum", "contractContent", "projectId", "user")
  val contractDAO: ContractDBDAO = mock(classOf[ContractDBDAO])
  val logDao: LogDao = mock(classOf[LogDao])
  val contractsManager: ContractsManager = new ContractsManager(configuration, contractDAO, logDao)
  val mockContractsManager: ContractsManager = mock(classOf[ContractsManager])
  val contractsList: File = File(testDirectory.pathAsString + "/ressources/contractsList")

  val bodyString = "{\"options\":{\"network\":\"ethereum\",\"version\":\"0.8.14\",\"evmVersion\":\"byzantium\",\"optimizerRuns\":200},\"migration\":[{\"name\":\"BigInt\",\"links\":[]},{\"name\":\"RSARingSignature\",\"links\":[\"BigInt\"],\"parameters\":[]},{\"name\":\"Forge\",\"parameters\":[],\"links\":[\"BigInt\",\"RSARingSignature\"]}]}"
  val body: JsValue = Json.parse(bodyString)
  val configFile: File = File(s"$projectTestDirectory/hardhat.config.js")
  val deploymentFile: File = File(s"$projectTestDirectory/ignition/modules/deploy.js")
  val contractOptions: ContractOptions = new ContractOptions(network = "ethereum", version = "0.8.14", evmVersion = Some("byzantium"), optimizerRuns = Some(200))
  val myFile: File = File(s"$testDirectory/ressources/contracts.zip")
  val tempFile: TemporaryFile = SingletonTemporaryFileCreator.create(myFile.path)

  val isWindows: Boolean = System.getProperty("os.name").toLowerCase().contains("windows")

  val contractsManagerSpy: ContractsManager = spy(contractsManager)
  val cm: ContractsManager = mock(classOf[ContractsManager])
  val credential: Credentials = mock(classOf[Credentials])

  "ContractsManagerTest" should {

    "createProject" in {
      val contractsManagerSpy: ContractsManager = spy(contractsManager)
      val project = File(projectTestDirectory)
      if (project.exists) project.delete()
      if (File(project.pathAsString + ".zip").notExists) myFile.copyTo(File(project.pathAsString + ".zip"))
      doReturn(true, Nil*).when(contractsManagerSpy).process(any[String], any[File], any[String], any[String])
      contractsManagerSpy.createProject(projectTestDirectory)
      project.list.nonEmpty
    }

    "migrate" in {
      if (isWindows) contractsManager.confirmDeployment mustEqual "$env:HARDHAT_IGNITION_CONFIRM_DEPLOYMENT='false'; " else contractsManager.confirmDeployment mustEqual "export HARDHAT_IGNITION_CONFIRM_DEPLOYMENT=false &&"
      doNothing().when(contractsManagerSpy).generateConfig(any[String], any[ContractOptions], any[String])(any[Credentials])
      doReturn(true, Nil*).when(contractsManagerSpy).process(any[String], any[File], any[String], any[String])
      println(s"$projectTestDirectory $projectTestId $contractOptions ethereum")
      contractsManagerSpy.migrate(projectTestDirectory, projectTestId, contractOptions, "ethereum")(credential) mustEqual true
    }

    "generateConfig without proxy" in {
      if (configFile.exists) configFile.delete()
      val config = mock(classOf[Configuration])
      val ec = mock(classOf[ECKeyPair])
      when(credential.getEcKeyPair).thenReturn(new ECKeyPair(BigInt(1234).bigInteger, BigInt(1234).bigInteger))
      when(ec.getPrivateKey).thenReturn(BigInt(1234).bigInteger)
      when(config.get[String]("blockchain.ethereum.url")).thenReturn("http://127.0.0.1:8545")
      contractsManager.generateConfig(projectTestDirectory, contractOptions, "ethereum")(credential)
      val trueString =
        """require("@nomicfoundation/hardhat-toolbox");
          |
          |module.exports = {
          |  networks: {
          |    ethereum: {
          |      url: "http://127.0.0.1:8545",
          |      accounts: ["4d2"]
          |    },
          |  },
          |  mocha: {
          |    // timeout: 100000
          |  },
          |  solidity: {
          |    version: "0.8.14",
          |    settings: {
          |      optimizer: {
          |        enabled: true,
          |        runs: 200,
          |      },
          |      evmVersion: "byzantium"
          |    }
          |  }
          |};"""
          .stripMargin
      configFile.contentAsString mustEqual trueString
    }

    "generateConfig with proxy" in {
      if (configFile.exists) configFile.delete()
      val config = mock(classOf[Configuration])
      val ec = mock(classOf[ECKeyPair])
      when(credential.getEcKeyPair).thenReturn(new ECKeyPair(BigInt(1234).bigInteger, BigInt(1234).bigInteger))
      when(ec.getPrivateKey).thenReturn(BigInt(1234).bigInteger)
      when(config.get[String]("blockchain.ethereum.url")).thenReturn("http://127.0.0.1:8545")
      val contractOptions = new ContractOptions(network = "ethereum", version = "0.8.14", evmVersion = Some("byzantium"), optimizerRuns = Some(200), Some("http://proxy.test:8080"), Some("http://proxy.test:8080"))
      contractsManager.generateConfig(projectTestDirectory, contractOptions, "ethereum")(credential)
      val trueString =
        """require("@nomicfoundation/hardhat-toolbox");
          |
          |const { ProxyAgent, setGlobalDispatcher } = require("undici");
          |const proxyAgent = new ProxyAgent('http://proxy.test:8080');
          |setGlobalDispatcher(proxyAgent);
          |
          |module.exports = {
          |  networks: {
          |    ethereum: {
          |      url: "http://127.0.0.1:8545",
          |      accounts: ["4d2"]
          |    },
          |  },
          |  mocha: {
          |    // timeout: 100000
          |  },
          |  solidity: {
          |    version: "0.8.14",
          |    settings: {
          |      optimizer: {
          |        enabled: true,
          |        runs: 200,
          |      },
          |      evmVersion: "byzantium"
          |    }
          |  }
          |};"""
          .stripMargin
      configFile.contentAsString mustEqual trueString
    }

    "uploadFile" in {
      val project: File = File(testDirectory.pathAsString + "/projects")
      if (project.notExists) project.createDirectory()
      doNothing().when(contractsManagerSpy).createProject(any[String])
      doNothing().when(contractsManagerSpy).createHeader(any[File])
      doNothing().when(contractsManagerSpy).createDeploy(any[String], any[JsValue], any[File])
      when(cm.migrate(s"$testDirectory/ressources/$projectTestId", projectTestId, contractOptions, "ethereum")(credential)).thenReturn(true)
      when(cm.proxyConfiguration(s"$testDirectory", projectTestId, contractOptions)).thenReturn(true)
      when(cm.installNpmDependencies(s"$testDirectory", projectTestId)).thenReturn(true)
      // doReturn(true,Nil: _*).when(contractsManagerSpy).installNpmDependencies(s"$testDirectory/ressources/contracts.zip")
      //doReturn( true, Nil: _*).when(contractsManagerSpy).migrate(s"$testDirectory/ressources/contracts.zip", contractOptions, "ethereum")(credential)
      doReturn(true, Nil*).when(contractsManagerSpy).process(any[String], any[File], any[String], any[String])
      doReturn(ListBuffer(contract, contract), Nil*).when(contractsManagerSpy).createContracts(any[String], any[String], any[String], any[String])
      val result = Await.result(contractsManagerSpy.uploadFile(tempFile, body, "projectTest", "userid", "ethereum")(credential), Duration.Inf)
      result mustEqual ListBuffer(contract, contract)
    }

    "createHeader" in {
      if (deploymentFile.exists) deploymentFile.delete()
      contractsManager.createHeader(deploymentFile)
      val trueString =
        s"""|const { buildModule } = require("@nomicfoundation/hardhat-ignition/modules");
            |
            |module.exports = buildModule("DeployerModule", (m) => {
            |
            |""".stripMargin
      deploymentFile.contentAsString mustEqual trueString
    }

    "createDeploy" in {
      contractsManager.createDeploy(projectTestDirectory, body, deploymentFile)
      val trueString =
        """|const { buildModule } = require("@nomicfoundation/hardhat-ignition/modules");
           |
           |module.exports = buildModule("DeployerModule", (m) => {
           |
           |  const BigInt = m.library("BigInt");
           |
           |  const RSARingSignature = m.contract("RSARingSignature", [], {
           |    libraries: { BigInt },
           |  });
           |
           |  const Forge = m.contract("Forge", [], {
           |    libraries: { BigInt },
           |    after: [ RSARingSignature ]
           |  });
           |
           |  return { BigInt, RSARingSignature, Forge, };
           |});
           |"""
          .stripMargin
      deploymentFile.contentAsString.replaceAll("\r", "") mustEqual trueString.replaceAll("\r", "")
    }

    "createContracts" in {
      when(contractDAO.create(any[Contract])).thenReturn(Future.successful(contract))
      val response = contractsManager.createContracts(contractsList.pathAsString, "ethereum", "idProject", "")
      debug("createContracts", "RESPONSE = " + response)
      val repo = contractsList / "ignition" / "deployments" / "chain-1337" / "artifacts"
      verify(contractDAO, times(repo.list.length)).create(any[Contract])
      response.length mustEqual repo.list.length
    }

    "createContract" in {
      when(contractDAO.create(any[Contract])).thenReturn(Future.successful(contract))
      val result = Await.result(contractsManager.createContract(contract), Duration.Inf)
      result._id.get.matches("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}") mustEqual true
    }

    "getContractsByProject" in {
      when(contractDAO.findBy("project")("projectId")).thenReturn(Future.successful(Seq(contract, contract)))
      val result = Await.result(contractsManager.getContractsByProject("projectId"), Duration.Inf)
      result.head._id mustEqual contract._id
      result.head.name mustEqual contract.name
      result.head.file mustEqual contract.file
      result.head.project mustEqual contract.project
      result.head.owner mustEqual contract.owner
    }

    "getContractsByUserId" in {
      when(contractDAO.findBy("owner")("user")).thenReturn(Future.successful(Seq(contract, contract)))
      val result = Await.result(contractsManager.getContractsByUserId("user"), Duration.Inf)
      result.head._id mustEqual contract._id
      result.head.name mustEqual contract.name
      result.head.file mustEqual contract.file
      result.head.project mustEqual contract.project
      result.head.owner mustEqual contract.owner
    }
  }
}
