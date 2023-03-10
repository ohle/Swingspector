![Swingspector](https://raw.githubusercontent.com/ohle/idea-swag/main/Logo.png)

![Build](https://github.com/ohle/idea-swag/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/18345.svg)](https://plugins.jetbrains.com/plugin/18345)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/18345.svg)](https://plugins.jetbrains.com/plugin/18345)

## Summary

<!-- Plugin description -->
`Swingspector` is a plugin for Intellij IDEA that allows to analyze Java Swing components in a running
application.

It is heavily inspired by the
standalone [Swing Explorer](https://github.com/swingexplorer/swingexplorer). (Swing Explorer does
come with an Intellij plugin, but it does not offer many features). In fact, the name of the
[instrumentation agent](https://github.com/ohle/swag) (SWAG, for SWing AGent), is blatantly copied
from that project.
<!-- Plugin description end -->

## Features

- Display information about swing components in a running application
- Display component hierarchy
- Navigate to point code where components were added

## Usage

![Demo](https://raw.githubusercontent.com/ohle/idea-swag/main/screenshots/Demo.gif)

`Swingspector` adds a new type of run configuration ("SWAG Swing Application") to IDEA. It is mostly
identical to the standard Java Application run configuration, but will instrument the application
for use with the plugin.

Create a new run configuration of type "SWAG Swing Application" for your application, then run your
application through it. `Swingspector` can now open a tool window with information about swing
components of the running application.

### Navigation

There are two ways to open a tool window for a component:

#### Using the hotkey

The run configuration setup dialog includes a keyboard shortcut (default <kbd>F12</kbd>). This
shortcut is installed in the application being run (so be sure to choose one that doesn't shadow an
important shortcut of the application itself).

Position your mouse cursor over a component of interest and press the shortcut keys (with the
keyboard focus in your application) to open a window for that component.

#### Using the hierarchy of the root windows

In the Run tool window, there is an additional tab ("SWAG root windows") that shows a list of all
the root windows (JFrames, Dialogs, …) that the application opened. Double-clicking one of those
will open the Swing Hierarchy tool window, showing the tree of components within that window.
Individual components can be opened from there.

If the list of windows is empty, press the refresh button once the application is actually showing a
window.

### The component tool window

![toolwindow](https://raw.githubusercontent.com/ohle/idea-swag/main/screenshots/toolwindow.png)
The window contains three sections:

- The placement panel, with information about the way the component was added to the hierarchy,
  including a stacktrace with clickable source links
- The appearance panel, showing a table of sizes at the top (with a pin if a size was set
  explicitly)
  and an interactive view of the component
- The properties panel, with various properties of the component grouped into thematic tabs. The
  listeners registered on the component (in the listeners tab) can be opened by clicking on them[^1]

[^1]: This currently only works for regular classes (including inner classes and anonymous classes),
but not method references or lambda expressions.

The toolbar has buttons to open the component in the hierarchy tree, and to open the component's
parent.

Immediate child components can be opened by double-clicking. When hovering over a child component,
its preferred, minimum, and maximum size are plotted, and its name is displayed in the overlay.

## Installation

- Using IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "
  Swingspector"</kbd> >
  <kbd>Install Plugin</kbd>

- Manually:

  Download the [latest release](https://github.com/ohle/idea-swag/releases/latest) and install it
  manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from
  disk...</kbd>

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
