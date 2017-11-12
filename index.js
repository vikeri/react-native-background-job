"use strict";
import { NativeModules, AppRegistry, Platform } from "react-native";

const AppState = NativeModules.AppState;
const tag = "BackgroundJob:";
const jobModule = Platform.select({
  ios: {},
  android: NativeModules.BackgroundJob
});
const jobs = {};
var globalWarning = __DEV__;

const BackgroundJob = {
  NETWORK_TYPE_UNMETERED: jobModule.UNMETERED,
  NETWORK_TYPE_ANY: jobModule.ANY,
  NETWORK_TYPE_NONE: -1,
  /**
     * Registers the job and the functions they should run.
     *
     * This has to run on each initialization of React Native and it has to run in the global scope and not inside any
     * component life cycle methods. See example project. Only registering the job will not schedule the job.
     * It has to be scheduled by `schedule` to start running.
     *
     * @param {Object} obj
     * @param {string} obj.jobKey A unique key for the job
     * @param {function} obj.job The JS-function that will be run
     *
     * @example
     * import BackgroundJob from 'react-native-background-job';
     *
     * const backgroundJob = {
     *  jobKey: "myJob",
     *  job: () => console.log("Running in background")
     * };
     *
     * BackgroundJob.register(backgroundJob);
     *
     */
  register: function({ jobKey, job }) {
    const existingJob = jobs[jobKey];

    if (!existingJob || !existingJob.registered) {
      var fn = async () => {
        job();
      };

      AppRegistry.registerHeadlessTask(jobKey, () => fn);

      if (existingJob) {
        jobs[jobKey].registered = true;
      } else {
        jobs[jobKey] = { registered: true, job };
      }
    }
  },

  /**
     * Schedules a new job.
     *
     * This only has to be run once while `register` has to be run on each initialization of React Native.
     *
     * @param {Object} obj
     * @param {string} obj.jobKey A unique key for the job that was used for registering, and be used for canceling in later stage.
     * @param {number} [obj.timeout = 2000] The amount of time (in ms) after which the React instance should be terminated regardless of whether the task has completed or not.
     * @param {number} [obj.period = 900000] The frequency to run the job with (in ms). This number is not exact, Android may modify it to save batteries. Note: For Android > N, the minimum is 900 0000 (15 min).
     * @param {boolean} [obj.persist = true] If the job should persist over a device restart.
     * @param {boolean} [obj.override = true] Whether this Job should replace pre-existing jobs with the same key.
     * @param {number} [obj.networkType = NETWORK_TYPE_NONE] Only run for specific network requirements.
     * @param {boolean} [obj.requiresCharging = false] Only run job when device is charging, (not respected by pre Android N devices) [docs](https://developer.android.com/reference/android/app/job/JobInfo.Builder.html#setRequiresCharging(boolean))
     * @param {boolean} [obj.requiresDeviceIdle = false] Only run job when the device is idle, (not respected by pre Android N devices) [docs](https://developer.android.com/reference/android/app/job/JobInfo.Builder.html#setRequiresDeviceIdle(boolean))
     * @param {boolean} [obj.exact = false] Schedule an job to be triggered precisely at the provided period. Note that this is not power-efficient way of doing things.
     * @param {boolean} [obj.allowWhileIdle=false] Allow the scheduled job to execute also while it is in doze mode.
     * @param {boolean} [obj.allowExecutionInForeground = false]  Allow the scheduled job to be executed even when the app is in foreground. Use it only for short running jobs.
     *
     * @example
     * import BackgroundJob from 'react-native-background-job';
     *
     * const backgroundJob = {
     *  jobKey: "myJob",
     *  job: () => console.log("Running in background")
     * };
     *
     * BackgroundJob.register(backgroundJob);
     *
     * var backgroundSchedule = {
     *  jobKey: "myJob",
     * }
     *
     * BackgroundJob.schedule(backgroundSchedule);
     */
  schedule: function({
    jobKey,
    timeout = 2000,
    period = 900000,
    persist = true,
    override = true,
    networkType = this.NETWORK_TYPE_NONE,
    requiresCharging = false,
    requiresDeviceIdle = false,
    exact = false,
    allowWhileIdle = false,
    allowExecutionInForeground = false
  }) {
    const savedJob = jobs[jobKey];

    if (!savedJob) {
      console.error(
        `${tag} The job ${jobKey} has not been registered, you must register it before you can schedule it.`
      );
    } else {
      jobModule.schedule(
        jobKey,
        timeout,
        period,
        persist,
        override,
        networkType,
        requiresCharging,
        requiresDeviceIdle,
        exact,
        allowWhileIdle,
        allowExecutionInForeground,
        scheduled => {
          console.log(
            `The job ${jobKey} was ${scheduled
              ? "successfully" 
              : "unsuccessfully"} scheduled.`
          );
        }
      );
    }
  },
  /**
   * Cancel a specific job
   *
   * @param {Object} obj
   * @param {string} obj.jobKey The unique key for the job
   *
   * @example
   * import BackgroundJob from 'react-native-background-job';
   *
   * BackgroundJob.cancel({jobKey: 'myJob'});
   */
  cancel: function({ jobKey }) {
    jobModule.cancel(jobKey, canceled => {
      console.log(
        `The job ${jobKey} was ${
          canceled ? "successfully" : "unsuccessfully"
        } canceled.`
      );
    });
  },
  /**
     * Cancels all the scheduled jobs
     *
     * @example
     * import BackgroundJob from 'react-native-background-job';
     *
     * BackgroundJob.cancelAll();
     */
  cancelAll: function() {
    jobModule.cancelAll(canceled => {
      console.log(
        `All the jobs were ${canceled
          ? "successfully"
          : "unsuccessfully"} canceled.`
      );
    });
    const keys = Object.keys(jobs);
  },
  /**
     * Sets the global warning level
     *
     * @param {boolean} warn
     *
     * @example
     * import BackgroundJob from 'react-native-background-job';
     *
     * BackgroundJob.setGlobalWarnings(false);
     *
     */
  setGlobalWarnings: function(warn) {
    globalWarning = warn;
  },
  /**
   * Checks Whether app is optimising battery using Doze,returns Boolean.
   *
   * @param {Callback} callback gets called with according parameters after result is received from Android module.
   * @example
   * import BackgroundJob from 'react-native-background-job';
   *
   * BackgroundJob.isAppIgnoringBatteryOptimisation((error,ignoringOptimization)=>{});
   */
  isAppIgnoringBatteryOptimization: function(callback) {
    jobModule.isAppIgnoringBatteryOptimization(ignoringOptimization => {
      if (ignoringOptimization != null) {
        callback("", ignoringOptimization);
      } else {
        callback("error", null);
      }
    });
  }
};
if (Platform.OS == "ios") {
  Object.keys(BackgroundJob).map(v => {
    BackgroundJob[v] = () => {
      if (globalWarning) {
        console.warn(
          "react-native-background-job is not available on iOS yet. See https://github.com/vikeri/react-native-background-job#supported-platforms"
        );
      }
    };
  });
}
module.exports = BackgroundJob;
