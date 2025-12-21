<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Swingspector Changelog

## [Unreleased]

## [2.1.0] - 2025-12-21

- Fix layout info in placement description
- More informative placement info for `GridBagLayout`
- Show layout managers in tree view
- Optionally show min/max/preferred/actual width and height in tree view

## [2.0.6]

- Compatibility with JDK 21+

## [2.0.5]

- Compatibility with IDEA 2024.2 (and up)

## [2.0.4]

- Compatibility with IDEA 2024.1

## [2.0.3]

### Added

- Support for non-Application run configurations (e.g. gradle)
- Connection timeouts are now configurable

### Changed

- When a connection times out, a balloon notification is shown (previously threw
  exception)

### Fixed

- Fix some deadlock situations when the application is unresponsive or stopped in debugger

## [2.0.2]

- (hopefully) fix agent loading bug on Windows

## [2.0.1]

- Fix EDT violation

## [2.0.0]

- Swingspector now plugs into regular Application RunConfigurations instead of providing a new type
- Existing Swingspector or SWAG RunConfigurations are now invalid; please create Application
  RunConfigurations
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

## [0.1.0]

### Added

- Run configuration for Swing applications
- Tool window showing details about Swing components in the running application
    - Maximum, minimum, preferred and actual sizes
    - Placement information, including stacktrace of Container.add() invocation
    - Various property values
    - Listeners with (limited) navigation
- Tree view of component hierarchy for root windows of the running application

[Unreleased]: https://github.com/ohle/Swingspector/compare/v2.1.0...HEAD
[2.1.0]: https://github.com/ohle/Swingspector/compare/v2.0.6...v2.1.0
[2.0.6]: https://github.com/ohle/Swingspector/compare/v2.0.5...v2.0.6
[2.0.5]: https://github.com/ohle/Swingspector/compare/v2.0.4...v2.0.5
[2.0.4]: https://github.com/ohle/Swingspector/compare/v2.0.3...v2.0.4
[2.0.3]: https://github.com/ohle/Swingspector/compare/v2.0.2...v2.0.3
[2.0.2]: https://github.com/ohle/Swingspector/compare/v2.0.1...v2.0.2
[2.0.1]: https://github.com/ohle/Swingspector/compare/v2.0.0...v2.0.1
[2.0.0]: https://github.com/ohle/Swingspector/compare/v1.1.0...v2.0.0
[1.1.0]: https://github.com/ohle/Swingspector/compare/v1.0.4...v1.1.0
[1.0.4]: https://github.com/ohle/Swingspector/compare/v1.0.3...v1.0.4
[1.0.3]: https://github.com/ohle/Swingspector/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/ohle/Swingspector/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/ohle/Swingspector/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/ohle/Swingspector/compare/v0.1.1...v1.0.0
[0.1.1]: https://github.com/ohle/Swingspector/compare/v0.1.0...v0.1.1
[0.1.0]: https://github.com/ohle/Swingspector/commits/v0.1.0
