# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Types of changes

`Added ` for new features.
`Changed` for changes in existing functionality.
`Deprecated` for soon-to-be removed features.
`Removed` for now removed features.
`Fixed` for any bug fixes.
`Security` in case of vulnerabilities.

## [v0.5.0b0] - 2024-03-15

### Added

- dag model export
- Model Management (Publishing, Deactivating, Deprecating, Deleting)
- Model HTTP(SPI) feature service management
- secreflow model export, GLM optimization, the ability to save additional feature columns to the results of
  the prediction operator, the binning modification function, and the PSI upgrade 0.0.2
- kusica 0.6.0 serving

### Fixed

- Data synchronization optimization reduces resource consumption
- log optimization
- springboot3.1.5 upgrade 3.19

## [v0.4.1b1] - 2024-02-02

### Fixed

- Fix edge node user update password url 404
- Fix graph edge del error
- Fix tee result download error

## [v0.4.1b0] - 2024-1-26

### Added

- Added 10+ components, such as feature calculation, cross-decision, outlier handling, grouping statistics, binning
  modification, GLM model training/prediction, to support the whole link of insurance pricing
- The model prediction (SecureBoost),glm component is optimized to support batch prediction
- WOE binning and general binning added report output and configuration, added error feature columns to the prediction
  component, and supported PSI 0.0.2

### Fixed

- Fix the missing SGD prediction tag column

## [v0.4.0b0] - 2024-1-12

### Added

- The platform supports P2P deployment architecture, supports project cooperation from the end side, and does not rely
  on
  trusted third parties
- Node routing support P2P mode

### Fixed

- Configuration parameters are added to the existing components to facilitate more flexible construction of training
  tasks
- In the P2P mode, the creation of platform projects requires the approval of the invitee
- The model prediction (SecureBoost) component is optimized to support batch prediction

## [0.3.0b0] - 2023-11-12

### Added

- Center Platform (Central Platform) adds Hub Mode (i.e., TEE's centralized computing mode):<br>
  ① Added 15 new TEE operator components;<br>
  ② Edge-side data can be directly uploaded to the simulation TEE within the deployment package (simulation version TEE)
  ;<br>
  ③ In Hub Mode, results must be reviewed before they can be downloaded;<br>

- Edge Platform (Node Platform) has been added, which users can deploy separately and then access to manage data and
  cooperative nodes. The management interfaces for built-in nodes Alice and Bob can still be accessed quickly from the
  Center Platform.

- Edge Platform adds a Message Center module, where cooperative node invitations and Hub Mode results downloads must be
  operated and reviewed within the Message Center module;

- Overall UI interface display optimization.

## [0.2.0b0] - 2023-9-6

### Added

- Project management：project creation, project node addition, project deletion, project modification.
- Data management: data upload and download, data table authorization, model and rule download.
- Task management: executing tasks, stopping tasks, viewing task logs, viewing task status, and viewing task results
- Supports 17 Secretflow operators of financial risk control.
- Node management: node registration and viewing node information.
- Other functions optimization.
