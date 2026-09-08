export type TaskMode = 'mode1' | 'mode2' | 'mode3';

export interface Mode1Config {
  browserDuration: number; // seconds
  repeatCount: number;
}

export interface Mode2Config {
  taskDuration: number; // seconds
  taskRepeatCount: number; // repeat without closing browser
  operationRepeatCount: number; // full open/close cycles
}

export interface Mode3Config {
  completionKeywords: string[];
  operationRepeatCount: number;
}

export interface Task {
  id: string;
  name: string;
  url: string;
  referer: string;
  utmSource?: string;
  utmMedium?: string;
  utmCampaign?: string;
  userAgent: string;
  mode: TaskMode;
  mode1Config?: Mode1Config;
  mode2Config?: Mode2Config;
  mode3Config?: Mode3Config;
  repeatCount: number;
  enabled: boolean;
  status: 'pending' | 'running' | 'completed' | 'error' | 'paused';
  completedRuns: number;
  lastLeadStatus?: 'lead' | 'no_lead' | 'unknown';
  totalLeads?: number;
}

export interface ProxyConfig {
  type: 'none' | 'http' | 'https' | 'socks4' | 'socks5';
  host: string;
  port: string;
  username: string;
  password: string;
  listUrl: string;
  rotation: boolean;
  currentIndex?: number;
  loadedProxies?: string[];
}

export interface CaptchaConfig {
  service: 'none' | '2captcha' | 'anticaptcha' | 'capsolver' | 'deathbycaptcha';
  apiKey: string;
}

export interface AppSettings {
  waitBetweenTasks: number; // seconds
  captcha: CaptchaConfig;
  proxy: ProxyConfig;
  cpaGripUserId: string;
  cpaGripKey: string;
}

export interface ExtractedInfo {
  ip: string;
  country: string;
  countryCode: string;
  city: string;
  region: string;
  street?: string;
  timezone: string;
  language: string;
  isp: string;
  org: string;
  latitude?: number;
  longitude?: number;
  postalCode?: string;
  currency?: string;
  asn?: string;
  callingCode?: string;
  flag?: string;
  connectionType?: string;
  proxyDetected?: boolean;
}

export interface GeneratedIdentity {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  address: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  birthDate: string;
  gender: string;
  cardType: string;
  cardNumber: string;
  cardExpiry: string;
  cardCvv: string;
  cardHolder: string;
  bankName: string;
  username?: string;
  password?: string;
}

export interface LogEntry {
  id: string;
  timestamp: Date;
  level: 'info' | 'warning' | 'error' | 'success';
  message: string;
  taskId?: string;
  taskName?: string;
}

export interface Script {
  id: string;
  name: string;
  code: string;
  timing: 'before' | 'after';
  execution?: 'parallel' | 'sequential';
  enabled: boolean;
}

export interface AutomationState {
  isRunning: boolean;
  currentTaskIndex: number;
  currentTask: Task | null;
  phase: 'idle' | 'preparing' | 'browser' | 'executing' | 'checking' | 'completed';
  phaseDetail: string;
  startTime?: Date;
  totalRuns?: number;
  successfulLeads?: number;
}

export interface LeadCheckResult {
  checked: boolean;
  isLead: boolean;
  ip: string;
  timestamp: Date;
  taskName?: string;
  taskId?: string;
}

export interface TaskStats {
  taskId: string;
  taskName: string;
  totalRuns: number;
  leads: number;
  noLeads: number;
  errors: number;
  conversionRate: number;
}
