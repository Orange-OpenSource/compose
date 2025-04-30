# Deploying Compose without Cert-Manager

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [Credential Configuration](#2-credential-configuration)
3. [NGINX Gateway Fabric](#3-nginx-gateway-fabric)
4. [Amazon Elastic Container Registry](#4-amazon-elastic-container-registry)
5. [Compose Deployment](#5-compose-deployment)
6. [Troubleshooting](#6-troubleshooting)
7. [Versions](#7-versions)
8. [Ressources](#8-ressources)

## 1. Prerequisites

Before starting, ensure the following requirements are met:

1. Install the following tools (refer to [Versions section](#7-versions) for more informations):
   - AWS CLI
   - Kubectl
   - Helm
2. Ensure you have access to an AWS account with the necessary permissions to create and manage resources.
3. Ensure the [NGINX Gateway Fabric](#3-nginx-gateway-fabric) is installed.

## 2. Credential Configuration

### 2.1 Retrieve AWS Credentials

Access the necessary AWS credentials from: [Orange Group AWS](https://orangegroup.awsapps.com/start/#/?tab=accounts).

### 2.2 Configure AWS CLI

Run the following command to configure the AWS CLI with AWS IAM Identity Center (formerly AWS SSO) to retrieve credentials for running AWS CLI commands:

```bash
aws configure sso --profile compose-admin
```

When prompted, provide the following values for the variables:

| Variable Name             | Description             | Example Value                             |
| :------------------------ | :---------------------- | :---------------------------------------- |
| `SSO session name`        | Name of the SSO session | `aws-compose`                             |
| `SSO start URL`           | AWS SSO URL             | `https://orangegroup.awsapps.com/start/#` |
| `SSO region`              | AWS region              | `eu-west-3`                               |
| `SSO registration scopes` | Scope of permissions    | `sso:account:access`                      |

> **Note**:
>
> - *If you are unsure about any of the values, refer to the [Orange Group AWS Portal](https://orangegroup.awsapps.com/start/#/?tab=accounts) or contact your administrator.*
>
> - *If you encounter issues in the next steps, you can manually edit the* `~/.aws/credentials` *file or export the credentials as environment variables:*
>
>   - *Edit* `~/.aws/credentials` *file:*
>
>     ```bash
>     [compose-admin]
>     aws_access_key_id=<your-access-key-id>
>     aws_secret_access_key=<your-secret-access-key>
>     aws_session_token=<your-session-token>
>     ```
>
>   - *Export as environment variables:*
>
>     ```bash
>     export AWS_ACCESS_KEY_ID=<your-access-key-id>
>     export AWS_SECRET_ACCESS_KEY=<your-secret-access-key>
>     export AWS_SESSION_TOKEN=<your-session-token>
>     ```
>
>     *Replace* `<your-access-key-id>`*,* `<your-secret-access-key>`*, and* `<your-session-token>` *with the values available on the [Orange Group AWS Portal](https://orangegroup.awsapps.com/start/#/?tab=accounts).*

### 2.3 Log in to AWS SSO

Authenticate with AWS SSO:

```bash
aws sso login --profile compose-admin
```

> **Note**: *Ensure your SSO session is active before proceeding. If the session expires, re-run the command.*

### 2.4 Update Kubernetes Configuration

Update the Kubernetes configuration (kubeconfig file) to connect to the EKS cluster:

```bash
aws eks update-kubeconfig --region eu-west-3 --name composeCluster --profile compose-admin
```

Verify the connection to the cluster:

```bash
kubectl get nodes
```

## 3. NGINX Gateway Fabric

> **Important:** By default, Helm and manifests configure NGINX Gateway Fabric to use ports 80 and 443, which may impact other gateway listeners on these ports. To use different ports, update the configuration. Ensure a gateway resource with a valid listener is configured for NGINX Gateway Fabric to function.

### 3.1 Installation with Helm

1. Install the Gateway API resources:

    ```bash
    kubectl kustomize "https://github.com/nginx/nginx-gateway-fabric/config/crd/gateway-api/standard?ref=v1.6.2" | kubectl apply -f -
    ```

    > **Note**: *Replace* `v1.6.2` *with the latest version if necessary.*

2. Install the latest stable release of NGINX Gateway Fabric in the `nginx-gateway` namespace:

    ```bash
    helm install ngf oci://ghcr.io/nginx/charts/nginx-gateway-fabric --create-namespace -n nginx-gateway
    ```

    > **Note**: `ngf` *is the release name and can be customized. This name is prefixed to the Deployment name.*

3. Wait for the Deployment to be ready:

    - Add the --wait flag to the helm install command, or
    - Run the following command after installation:

      ```bash
      kubectl wait --timeout=5m -n nginx-gateway deployment/ngf-nginx-gateway-fabric --for=condition=Available
      ```

4. Retrieve the NLB (Network Load Balancer) DNS (Directory Name System) name to access NGINX Gateway Fabric. Use the following command to get the DNS name from the `EXTERNAL-IP` column:

    ```bash
    kubectl get svc ngf-nginx-gateway-fabric -n nginx-gateway
    ```

### 3.2 Installation with Manifests

1. Install the Gateway API resources:

    ```bash
    kubectl kustomize "https://github.com/nginx/nginx-gateway-fabric/config/crd/gateway-api/standard?ref=v1.6.2" | kubectl apply -f -
    ```

    > **Note**: *Replace* `v1.6.2` *with the latest version if necessary.*

2. Deploy the NGINX Gateway Fabric CRDs:

    ```bash
    kubectl apply -f https://raw.githubusercontent.com/nginx/nginx-gateway-fabric/v1.6.2/deploy/crds.yaml
    ```

    > **Note**: *Replace* `v1.6.2` *with the latest version if necessary.*

3. Deploy NGINX Gateway Fabric:

    For default deployments, run:

    ```bash
    kubectl apply -f https://raw.githubusercontent.com/nginx/nginx-gateway-fabric/v1.6.2/deploy/default/deploy.yaml
    ```

    > **Note**:
    > - *Replace* `v1.6.2` *with the latest version if necessary.*
    > - *By default, NGINX Gateway Fabric is installed in the nginx-gateway namespace. You can deploy in another namespace by modifying the manifest files.*
    > - *For custom environments, refer to the [official NGINX documentation](https://docs.nginx.com/nginx-gateway-fabric/installation/installing-ngf/manifests/).*

## 4. Amazon Elastic Container Registry

This section is only relevant **only if** you want to use Amazon ECR to store Docker images for deployment and you need to push new Docker images to Amazon ECR. If the required images are already available in ECR, you can skip these steps.

Additionally, you only need to re-authenticate with AWS SSO or Docker if your credentials or tokens have expired.

### 4.1 Authenticate with AWS SSO (if needed)

Re-authenticate with AWS SSO if your session has expired:

```bash
aws sso login --profile compose-admin
```

### 4.2 Authenticate Docker with ECR (if needed)

Re-authenticate Docker with ECR if your credentials have expired:

```bash
aws ecr get-login-password --region eu-west-3 --profile compose-admin | docker login --username AWS --password-stdin <AccountId>.dkr.ecr.eu-west-3.amazonaws.com
```

### 4.3 Pull, Tag, and Push Docker Images (if needed)

Follow these steps only if you need to push new images to ECR:

1. Pull the image from the source registry:

    ```bash
    docker pull <sourceRegistry>/<imagePath>:<myTag>
    ```

2. Tag the image for ECR:

   - For the backend:

        ```bash
        docker tag <sourceRegistry>/<imagePath>:<myTag> <AccountId>.dkr.ecr.eu-west-3.amazonaws.com/compose/backend/scaas:<tag>
        ```

   - For the frontend:

        ```bash
        docker tag <sourceRegistry>/<imagePath>:<myTag> <AccountId>.dkr.ecr.eu-west-3.amazonaws.com/compose/frontend/portal:<tag>
        ```

3. Push the image to ECR:

   - For the backend:

        ```bash
        docker push <AccountId>.dkr.ecr.eu-west-3.amazonaws.com/compose/backend/scaas:<tag>
        ```

   - For the frontend:

        ```bash
        docker push <AccountId>.dkr.ecr.eu-west-3.amazonaws.com/compose/frontend/portal:<tag>
        ```

> **Note**: *Replace* `<sourceRegistry>`*,* `<imagePath>`*,* `<myTag>`*,* `<AccountId>` *and* `<tag>` *with the appropriate values for your deployment.*

## 5. Compose Deployment

### 5.1 Prepare the Environment

Before deploying, update the `values.yaml` file with the following variables. These variables are required for configuring the deployment:

| Variable               | Description                     | Example Value                                                                         |
| :--------------------- | :------------------------------ | :------------------------------------------------------------------------------------ |
| `namespace`            | Namespace for the deployment    | `compose`                                                                             |
| `scaas.replicaCount`   | Number of SCAAS pods            | `1`                                                                                   |
| `scaas.image`          | SCAAS Docker image              | `<AccountId>.dkr.ecr.eu-west-3.amazonaws.com/compose/backend/scaas:dev`               |
| `scaas.baseUrl`        | SCAAS service base URL          | `backend.compose-orange.com`                                                          |
| `scaas.containerPort`  | SCAAS container port            | `9000`                                                                                |
| `scaas.secretKey`      | SCAAS authentication secret key | `"&?Dc$35LNJ2/j^iMr{dKFuM4*r50H#"`                                                    |
| `portal.replicaCount`  | Number of Portal pods           | `1`                                                                                   |
| `portal.image`         | Portal Docker image             | `<AccountId>.dkr.ecr.eu-west-3.amazonaws.com/compose/frontend/portal:dev`             |
| `portal.baseUrl`       | Portal base URL                 | `www.compose-orange.com`                                                              |
| `portal.containerPort` | Portal container port           | `8080`                                                                                |
| `portal.secretKey`     | JWT token secret key for Portal | `"j9E6\=2YBWaD&BmSY!/o"`                                                              |
| `mongo.replicaCount`   | Number of MongoDB pods          | `1`                                                                                   |
| `mongo.image`          | MongoDB Docker image            | `mongo:5.0`                                                                           |
| `mongo.baseUrl`        | MongoDB service hostname        | `mongo-scaas`                                                                         |
| `mongo.port`           | MongoDB service port            | `27017`                                                                               |
| `tls.frontend.crt`     | Frontend TLS certificate        | `-----BEGIN CERTIFICATE----- ... your certificate here ... -----END CERTIFICATE-----` |
| `tls.frontend.key`     | Frontend TLS private key        | `-----BEGIN PRIVATE KEY----- ... your private key here ... -----END PRIVATE KEY-----` |
| `tls.backend.crt`      | Backend  TLS certificate        | `-----BEGIN CERTIFICATE----- ... your certificate here ... -----END CERTIFICATE-----` |
| `tls.backend.key`      | Backend TLS private key         | `-----BEGIN PRIVATE KEY----- ... your private key here ... -----END PRIVATE KEY-----` |

> **Important**: Ensure all sensitive values (e.g., tokens, secret keys) are stored securely and not exposed in public repositories.

### 5.2 Deploy Compose with Helm

Once the `values.yaml` file is completed, deploy the application using the following command:

```bash
helm install compose . --namespace compose --create-namespace
```

> **Note**: *Use the* `--create-namespace` *option only if the specified namespace (*`compose` *in this example) does not already exist. You can check existing namespaces using* `kubectl get ns` *or* `kubectl get namespaces`.

If the application is already installed (you can check with `helm list -A`), do not run `helm install`. Instead, use the following command to upgrade the application:

```bash
helm upgrade compose . --namespace compose
```

### 5.3 Uninstall Compose with Helm

Uninstall the application using the following command:

```bash
helm uninstall compose
```

## 6. Troubleshooting

### 6.1 AWS CLI Errors

If you encounter issues with AWS CLI commands, ensure your credentials are correctly configured in `~/.aws/credentials` or exported as environment variables.

### 6.2 Kubernetes Errors

Use the following commands to debug issues with pods or deployments:

- Describe a pod or deployment:

    ```bash
    kubectl describe <resource> <name>
    ```

- Check pod logs:

    ```bash
    kubectl logs <pod-name>
    ```

> **Note**: *Replace* `<resource>`*,* `<name>`*, and* `<pod-name>` *with the appropriate values.*

## 7. Versions

The following tool versions were used during the deployment process described in this guide. Ensure you use compatible versions to avoid unexpected issues.

| Tool      | Version | Notes                                  |
| :-------- | :------ | :------------------------------------- |
| AWS CLI   | 2.24.24 | For managing AWS resources.            |
| Kubectl   | 1.32.1  | For managing Kubernetes clusters.      |
| Helm      | 3.17.2  | For deploying Kubernetes applications. |

> **Note**: *If you are using different versions, refer to the official documentation of each tool to ensure compatibility with the deployment process.*

## 8. Ressources

- Gateway API: <https://gateway-api.sigs.k8s.io/guides/#installing-gateway-api>
- NGINX Gateway Fabric: <https://docs.nginx.com/nginx-gateway-fabric/installation/installing-ngf/manifests/>
