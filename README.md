JPEGHULMDExtractorAndValidator

This plugin implements two interfaces: both FormatValidationPlugin and MDExtractorPlugin.

In order for this jar to be run for both purposes, we have two metadata xms configuration files under the plugin's /PLUGIN-INF/ directory. Each xml holds a different type and related configuration.
There should be one xml per interface.
