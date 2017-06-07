"use strict";
import {
  NativeModules,
  AppRegistry,
  Platform,
  DeviceEventEmitter
} from "react-native";

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
  /**
     * Registers jobs and the functions they should run.
     *
     * This has to run on each initialization of React Native and it has to run in the global scope and not inside any
     * component life cycle methods. See example project. Only registering the job will not start running the job.
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
      DeviceEventEmitter.addListener(jobKey, job);

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
     * @param {string} obj.jobKey A unique key for the job
     * @param {number} [obj.period = 900000] - The frequency to run the job with (in ms). This number is not exact, Android may modify it to save batteries. Note: For Android > N, the minimum is 900 0000 (15 min).
     * @param {boolean} [obj.persist = true] If the job should persist over a device restart.
     * @param {boolean} [obj.override = true] Whether this Job should replace pre-existing Jobs with the same key.
     * @param {number} [obj.networkType = BackgroundJob.NETWORK_TYPE_ANY] Only run for specific network requirements, (not respected by pre Android N devices) [docs](https://developer.android.com/reference/android/app/job/JobInfo.html#NETWORK_TYPE_ANY)
     * @param {boolean} [obj.requiresCharging = false] Only run job when device is charging, (not respected by pre Android N devices) [docs](https://developer.android.com/reference/android/app/job/JobInfo.Builder.html#setRequiresCharging(boolean))
     * @param {boolean} [obj.requiresDeviceIdle = false] Only run job when the device is idle, (not respected by pre Android N devices) [docs](https://developer.android.com/reference/android/app/job/JobInfo.Builder.html#setRequiresDeviceIdle(boolean))
     * @param {boolean} [obj.alwaysRunning = false] Creates a foreground service that will keep the app alive forever. Suitable for music playback etc. Will always show a notification.
     * @param {string} obj.notificationTitle The title of the persistent notification when `alwaysRunning`
     * @param {string} obj.notificationText The text of the persistent notification when `alwaysRunning`
     * @param {string} obj.notificationIcon The icon string (in drawable) of the persistent notification when `alwaysRunning`
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
    period = 900000,
    persist = true,
    override = true,
    networkType = this.NETWORK_TYPE_ANY,
    requiresCharging = false,
    requiresDeviceIdle = false,
    alwaysRunning = false,
    notificationTitle,
    notificationText,
    notificationIcon
  }) {
    const savedJob = jobs[jobKey];

    if (!savedJob) {
      console.error(
        `${tag} The job ${jobKey} has not been registered, you must register it before you can schedule it.`
      );
    } else {
      jobModule.schedule(
        jobKey,
        period,
        persist,
        overrid,
        networkType,
        requiresCharging,
        requiresDeviceIdle,
        alwaysRunning,
        notificationTitle,
        notificationIcon,
        notificationText
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
    // TODO: Add callback to the cancel method
    jobModule.cancel(jobKey);
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
    jobModule.cancelAll();
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
