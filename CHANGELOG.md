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


## [0.12.0b0] - 2024-12-15

### Added

- [Feature]  Added a SCQL component
- [Feature]  Added a PSI component for three parties
- [Feature]  Added a SQL component for one-side data preprocessing
- [Feature]  Added an Unbalanced PSI Cache component and an Unbalanced PSI calculate component
- [Feature]  Added bad_rate and total_rate metrics in the report of WOE component
- [Feature]  Added original_count and total_count metrics in the report of PSI component
- [Feature]  Added running progress display on training components
- [Documentation] Operation Manual for SCQL component

### Changed
- [Optimization] Improved MySQL datasource registration prompts

### Fixed
- [Fixed] data query issues in P2P deploy mode



## [0.11.0b0] - 2024-10-15

### Added

- [Feature]  Data Source Support for MySQL Database (Structured data tables only).
- [Feature]  io_read_data/io_write_data Support for sgb, glm, New Read Model, and Model Backtesting Component.
- [Feature]  Support for Reading ODPS Data Table Partitions.
- [Feature]  Support for Scheduled Periodic Tasks: Including configuration, task list, and result viewing.
- [Feature]  New Prometheus Monitoring Metrics.
- [Feature]  Specify Field Types When Granting Sample Tables to Projects.

### Changed

- [Compatible] Partial Code Refactoring Optimization.

## [0.10.0b0] - 2024-08-15

### Added

- [Feature]  Supports multi-node management in P2P deploy mode
- [Feature]  Added a data source management module with ODPS(MaxCompute) data source
- [Feature]  Supports self-define NUll value at data table registration
- [Feature]  Supports model pacakge and deployment by OSS or ODPS(MaxCompute) data source
- [Feature]  Supports XGB model serialization and deserialization
- [Feature]  Supports output received by single side receiver in psi component
- [Feature]  Added a express condition filter component
- [Feature]  Added a NULL filter parameter in condition filter component
- [Feature]  Added a new resident service named dataproxy to access different data sources
- [Documentation] Supplemental documentation for Secretpad incompatible change (incompatible_statement_cn.md)

### Changed

- [Incompatible] Refactor VoteRequestDO  [incompatible statement](./docs/development/incompatible_statement_cn.md)

## [0.9.0b0] - 2024-07-15

### Added

- [Feature] Added support for tweedie training in SGD component.
- [Feature] Multi-node architecture restructured with gRPC and logging/monitoring enhancements.

### Changed

- [Optimization] Single-sided operator support & proto protocol upgrade (dependent on Kuscia).

## [0.8.1b0] - 2024-06-28

### Fixed

- [Fixed] Fix incompatible sql issue

## [0.8.0b0] - 2024-06-15

### Added

- [Feature] Added a type conversion component.
- [Feature] Added a sampling component that supports stratified sampling, random sampling, and systematic sampling.
- [Feature] Added a scorecard assessment component to convert binary classification results into scores
- [Feature] Added a PSI (Population Stability Index) component to assess feature stability.
- [Feature] Added data source management module with OSS data source support.
- [Feature] Supports configuration of the default storage data source.
- [Feature] Adapted to Kuscia 0.9.0b0 version, supports data retention after reinstallation of SecretPad.

### Changed

- [Optimization] A prompt has been added that prohibits modifying the route during task execution.

### Fixed

- [Fixed] Fix the version prompt on the edge node in center deploy mode.
- [Fixed] Fix the official website documentation has some blank sections.

## [0.7.0b0] - 2024-05-15

### Added

- [Feature] Secretpad arm & amd image builds, MVP package supports arm & amd.
- [Feature] Canvas supports changes in SecretFlow 1.6.0: support for GLM model in pvalue evaluation, onehot encoding
  supports dropping the mode column, LR training components add an option to output weights, addition of vertical union
  operator for datasets, psi version upgrade, etc.
- [Feature] Support for changes in kuscia.sh, watch job interface changes, arm changes, and other kuscia 0.8.0
  modifications.
- [Feature] Canvas task parallel value configuration.
- [Feature] Front-end display of Secretpad component versions.
- [Feature] Integration with Alibaba Cloud SLS to display detailed logs of SLS components on the web interface.
- [Feature] Secretpad version upgrade data retention.
- [Feature] Serving online prediction release can specify system resources CPU, memory.
- [Feature] Addition of platform component linear model parameter modification (currently supports modification of glm
  model).
- [Documentation] Supplemental documentation for Secretpad and Secretpad frontend development.
- [Documentation] Secretpad GitHub technical documentation covering deployment, usage, common issues, development.

### Changed

- [Optimization] Kuscia grpc-related code refactoring and optimization.
- [Optimization] Adjustment and standardization of data initialization operations.
- [Optimization] Optimization of unit test structure, log output content, and format.

### Security

- [Vulnerability] Fixes for CVE-2022-40897, CVE-2024-20952, CVE-2024-20932, CVE-2024-20918, CVE-2024-22262,
  CVE-2024-29025.

## [0.6.0b0] - 2024-04-15

### Added

- add Local development and debugging documentation|Stable component version matching
- add Adaptation to MySQL (requires users to provide MySQL services)
- add SLS logs to the cloud (requires users to have Alibaba Cloud SLS)
- add no tls for kuscia network protocols
- add adapt kuscia v0.7.0 SecretFlow v1.5.0
- supports duplicate values of association keys for PSI component in platform

### Fixed

- fix When multiple parties participate in the execution of the canvas, more than 2 parties read the sample table and
  the participants are accurately matched
- fix Kuscia job watch timeout
- fix The serial number of the front-end initiating component has a status

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

## [v0.4.1b0] - 2024-01-26

### Added

- Added 10+ components, such as feature calculation, cross-decision, outlier handling, grouping statistics, binning
  modification, GLM model training/prediction, to support the whole link of insurance pricing
- The model prediction (SecureBoost),glm component is optimized to support batch prediction
- WOE binning and general binning added report output and configuration, added error feature columns to the prediction
  component, and supported PSI 0.0.2

### Fixed

- Fix the missing SGD prediction tag column

## [v0.4.0b0] - 2024-01-12

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

## [0.2.0b0] - 2023-09-06

### Added

- Project management：project creation, project node addition, project deletion, project modification.
- Data management: data upload and download, data table authorization, model and rule download.
- Task management: executing tasks, stopping tasks, viewing task logs, viewing task status, and viewing task results
- Supports 17 Secretflow operators of financial risk control.
- Node management: node registration and viewing node information.
- Other functions optimization.
