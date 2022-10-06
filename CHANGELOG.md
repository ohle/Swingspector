<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# idea-swag Changelog

## [Unreleased]

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