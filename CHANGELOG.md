<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Swingspector Changelog

## [Unreleased]
- Fix some deadlock situations when the Application is unresponsive or stopped in debugger
- Make connection timeout configurable; use balloon notification instead of exception in case of timeout

## [2.0.2]
- (hopefully) fix agent loading bug on Windows

## [2.0.1]
- Fix EDT violation

## [2.0.0]
- Swingspector now plugs into regular Application RunConfigurations instead of providing a new type
- Existing Swingspector or SWAG RunConfigurations are now invalid; please create Application RunConfigurations
  and enable Swingspector via "Modify Options" -> "Swing"

## [1.1.0]
- Avoid usages of internal APIs
- Compatibility with 2023.1

## [1.0.4]
- Fix occasional exceptions from missing context when IDEA refreshes some actions

## [1.0.3]
- Compatibility with IDEA 2022.3

## [1.0.2]
- Compatibility with IDEA 2022.2

## [1.0.1]
- Compatibility with IDEA 2022.1

## [1.0.0]
### Added
- Component view:
    - Rulers and pixel position overlay
    - Click and drag to measure distances
    - Show children's sizing rectangles on hover
    - double click to open child
- Open parent from action toolbar

### Fixed
- Hangs on components with weird min/max/preferred sizes
- Occasional exceptions on closing the IDE

## [0.1.1]
### Added
- Apache 2.0 License

### Changed
- Improved readme
- Logo

## 0.1.0
### Added
- Run configuration for Swing applications
- Tool window showing details about Swing components in the running application
    - Maximum, minimum, preferred and actual sizes
    - Placement information, including stacktrace of Container.add() invocation
    - Various property values
    - Listeners with (limited) navigation
- Tree view of component hierarchy for root windows of the running application