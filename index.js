'use strict';
import { NativeModules, AppRegistry } from 'react-native';

const tag = "BackgroundJob:";
const jobModule = NativeModules.BackgroundJob;
var nativeJobs = jobModule.jobs;

var jobs = {};

console.log("Starting");
console.log(jobModule, jobs);

const BackgroundJob = {

    /**
     * Registers jobs and the functions they should run. 
     * 
     * This has to run on each initialization of React Native. Only doing this will not start running the job. It has to be scheduled by `schedule` to start running.
     * 
     * @param {Object} obj
     * @param {string} obj.jobKey A unique key for the job
     * @param {function} obj.job The JS-function that will be run 
     */
    register: function ({jobKey, job}) {
        const existingJob = jobs[jobKey];

        if (!existingJob || !existingJob.registered) {
            var fn = async () => {
                job();
            };

            AppRegistry.registerHeadlessTask(jobKey, () => fn);

            if (existingJob) {
                jobs[jobKey].registered = true;
            } else {
                const scheduledJob = nativeJobs.filter((nJob) => nJob.jobKey == jobKey);
                const scheduled = scheduledJob[0] != undefined;
                jobs[jobKey] = { registered: true, scheduled };
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
     * @param {number} obj.timeout How long the JS job may run before being terminated by Android (in ms).
     * @param {number} [obj.period = 900000] - The frequency to run the job with (in ms). This number is not exact, Android may modify it to save batteries. Note: For Android > N, the minimum is 900 0000 (15 min).
     * @param {boolean} [obj.persist = true] If the job should persist over a device restart.
     * @param {boolean} [obj.warn = true] If a warning should be raised if overwriting a job that was already scheduled.
     * 
     * @example
     * import BackgroundJob from 'react-native-background-job';
     * 
     * var backgroundJob = {
     *  jobKey: "myJob",
     *  job: () => { console.log("Running in background"); },
     *  timeout: 5000
     * };
     * 
     * BackgroundJob.schedule(backgroundJob);
     */
    schedule: function ({jobKey, timeout, period = 900000, persist = true, warn = true}) {

        const savedJob = jobs[jobKey];

        if (!savedJob) {
            console.error(`${tag} The job ${jobKey} has not been registered, you must register it before you can schedule it.`);
        } else {

            if (savedJob.scheduled && warn && __DEV__) {
                console.warn(`${tag} Overwriting background job: ${jobKey}`);
            } else {
                jobs[jobKey].scheduled = true;
            }

            jobModule.schedule(jobKey, timeout, period, persist);
        }


    },

    /**
     * Fetches all the currently scheduled jobs
     * 
     * @param {Object} obj
     * @param {function(Array)} obj.callback A list of all the scheduled jobs will be passed to the callback
     * 
     * @example
     * import BackgroundJob from 'react-native-background-job';
     * 
     * BackgroundJob.getAll({callback: (jobs) => {
     *  console.log("Jobs:",jobs);
     * }});
     * 
     */
    getAll: function ({callback}) {
        jobModule.getAll(callback);
    },

    /**
     * Cancel a specific job
     * 
     * @param {Object} obj
     * @param {string} obj.jobKey The unique key for the job
     * @param {boolean} [obj.warn = true] If one tries to cancel a job that has not been scheduled it will warn
     * 
     * @example
     * import BackgroundJob from 'react-native-background-job';
     * 
     * BackgroundJob.cancel('myJob');
     */
    cancel: function ({jobKey, warn = true}) {
        if (__DEV__ && warn && (!jobs[jobKey] || !jobs[jobKey].scheduled)) {
            console.warn(`${tag} Trying to cancel the job ${jobKey} but it is not scheduled`);
        }
        jobModule.cancel(jobKey);
        jobs[jobKey] ? jobs[jobKey].scheduled = false : null;
    },

    /**
     * Cancels all the scheduled jobs
     * 
     * @example
     * import BackgroundJob from 'react-native-background-job';
     * 
     * BackgroundJob.cancelAll();
     */
    cancelAll: function () {
        jobModule.cancelAll();
        const keys = Object.keys(jobs);
        keys.map((key) => { jobs[key].scheduled = false });
    }

}
module.exports = BackgroundJob;