/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from "react";
import {
  AppRegistry,
  TouchableHighlight,
  StyleSheet,
  Text,
  View
} from "react-native";

import BackgroundJob from "react-native-background-job";

const myJobKey = "Hej";

// This has to run outside of the component definition since the component is never
// instantiated when running in headless mode
BackgroundJob.register({
  jobKey: myJobKey,
  job: () => console.log("Background Job fired!")
});

export default class backtest extends Component {
  constructor(props) {
    super(props);
    this.state = { jobs: [] };
  }

  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Testing BackgroundJob
        </Text>
        <Text style={styles.instructions}>
          Try connecting the device to the developer console, schedule an event and then quit the app.
        </Text>
        <Text>
          Scheduled jobs:
          {this.state.jobs.map(({ jobKey }) => jobKey)}
        </Text>
        <TouchableHighlight
          style={styles.button}
          onPress={() => {
            BackgroundJob.schedule({
              jobKey: myJobKey,
              period: 10,
              timeout: 10,
              networkType: BackgroundJob.NETWORK_TYPE_ANY
            });
          }}
        >
          <Text>Schedule</Text>
        </TouchableHighlight>
        <TouchableHighlight
          style={styles.button}
          onPress={() => {
            BackgroundJob.schedule({
              jobKey: myJobKey,
              period: 10,
              timeout: 10,
              networkType: BackgroundJob.NETWORK_TYPE_ANY,
              alwaysRunning: true
            });
          }}
        >
          <Text>Schedule always running job</Text>
        </TouchableHighlight>
        <TouchableHighlight
          style={styles.button}
          onPress={() => {
            BackgroundJob.cancel({ jobKey: myJobKey });
          }}
        >
          <Text>Cancel</Text>
        </TouchableHighlight>
        <TouchableHighlight
          style={styles.button}
          onPress={() => {
            BackgroundJob.cancelAll();
          }}
        >
          <Text>CancelAll</Text>
        </TouchableHighlight>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  button: { padding: 20, backgroundColor: "#ccc", marginBottom: 10 },
  container: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#F5FCFF"
  },
  welcome: { fontSize: 20, textAlign: "center", margin: 10 },
  instructions: { textAlign: "center", color: "#333333", marginBottom: 5 }
});

AppRegistry.registerComponent("backtest", () => backtest);
