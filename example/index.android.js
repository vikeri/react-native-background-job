/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from "react";
import {
  AppRegistry,
  AsyncStorage,
  ScrollView,
  TouchableHighlight,
  StyleSheet,
  DeviceEventEmitter,
  Text,
  View
} from "react-native";

import BackgroundJob from "react-native-background-job";

const myJobKey = "Hej";

// This has to run outside of the component definition since the component is never
// instantiated when running in headless mode
BackgroundJob.register({
  jobKey: myJobKey,
  job: hej
});
var counter = 0;
const dateString = new Date().toISOString();
function hej() {
  console.log("Running in BG");
  const lastDate = new Date().toISOString();
  // AsyncStorage.setItem(dateString, lastDate, console.log);
}

export default class backtest extends Component {
  getLocal() {
    console.log("Getting local");
    // AsyncStorage.getAllKeys((err, keys) => {
    //   console.log("ASYNC", this);
    //   console.log("KEYS:");
    //   console.log(keys);
    //   keys.map(key =>
    //     AsyncStorage.getItem(key, (err, val) => {
    //       console.log(val);
    //       let ob = this.state.dates;
    //       ob[key] = val;
    //       this.setState({ dates: ob });
    //     }));
    // });
  }
  constructor(props) {
    super(props);
    this.state = {
      jobs: [],
      dates: {}
    };
  }

  getAll() {
    BackgroundJob.getAll({
      callback: jobs => {
        this.setState({ jobs });
      }
    });
  }

  render() {
    return (
      <ScrollView>
        <View style={styles.container}>
          <Text style={styles.welcome}>
            Testing BackgroundJob Now
          </Text>
          <Text style={styles.instructions}>
            {Object.keys(this.state.dates).map(date => {
              return "start: " +
                date.substr(11, 8) +
                "\nstop: " +
                this.state.dates[date].substr(11, 8) +
                "\n";
            })})}
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
                period: 5000,
                alwaysRunning: true,
                timeout: 0
              });
              this.getAll();
            }}
          >
            <Text>Schedule</Text>
          </TouchableHighlight>
          <TouchableHighlight
            style={styles.button}
            onPress={() => {
              AsyncStorage.clear();
            }}
          >
            <Text>Clear Storage</Text>
          </TouchableHighlight>
          <TouchableHighlight
            style={styles.button}
            onPress={() => {
              BackgroundJob.cancelAll();
              this.getAll();
            }}
          >
            <Text>CancelAll</Text>
          </TouchableHighlight>
          <TouchableHighlight
            style={styles.button}
            onPress={this.getAll.bind(this)}
          >
            <Text>GetAll</Text>
          </TouchableHighlight>
        </View>
      </ScrollView>
    );
  }
  componentDidMount() {
    this.getAll();
    // setInterval(this.getLocal.bind(this), 1000);
    this.getLocal();
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
