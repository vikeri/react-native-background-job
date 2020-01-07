export interface ICancelOption {
	jobKey: string;
}
export interface IRegisterOptions extends ICancelOption {
	job: (taskData: any) => Promise<void>;
}
export interface IScheduleOptions extends ICancelOption {
	timeout?: number;
	period?: number;
	persist?: boolean;
	override?: boolean;
	networkType?: number;
	requiresCharging?: boolean;
	requiresDeviceIdle?: boolean;
	exact?: boolean;
	allowWhileIdle?: boolean;
	allowExecutionInForeground?: boolean;
	notificationText?: string;
	notificationTitle?: string;
}

export function register(option: IRegisterOptions): void;

export function schedule(option: IScheduleOptions): Promise<void>;

export function cancel(option: ICancelOption): Promise<void>;

export function cancelAll(): Promise<void>;

export function setGlobalWarnings(warn: boolean): void;

export function isAppIgnoringBatteryOptimization(callback: (err: string, isIgnoring: boolean) => void): Promise<boolean>;