# Kubernetes Deployment

This repository contains the deployment guides and Helm charts for deploying Compose to a Kubernetes cluster.

## Helm Charts

Two Helm charts are available:

- **[compose-cert-manager](compose-cert-manager/):** Deploys Compose with Cert-Manager for automatic TLS certificate management.
  - See the guide: [deploying_with_cert_manager.md](docs/deploying_with_cert_manager.md)
- **[compose-no-cert-manager](compose-no-cert-manager/):** Deploys Compose without Cert-Manager. You will need to provide your own TLS certificates.
  - See the guide: [deploying_without_cert_manager.md](docs/deploying_without_cert_manager.md)
