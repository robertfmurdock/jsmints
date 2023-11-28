import path from "path";
import fs from "fs";

const reportDirectory = path.relative('./', process.env.REPORT_DIR) + "/"
const testResultsDir = path.relative('./', process.env.TEST_RESULTS_DIR) + "/"
const logDir = path.relative('./', process.env.LOGS_DIR) + "/"

const options = {
    baseUrl: '@BASE_URL@'
}

const directories = {
    reports: reportDirectory,
    testResults: testResultsDir,
    logs: logDir,
}

const reporters = [
    'dot',
    ['junit', {
        outputDir: testResultsDir,
        outputFileFormat: (options) => `results.xml`
    }],
];

let incubatingConfig = {
    runner: 'local',
    specs: [
        process.env.SPEC_FILE
    ],
    sync: false,
    exclude: [],
    maxInstances: 1,
    capabilities: [],
    logLevel: 'warn',
    bail: 0,
    outputDir: directories.logs,
    baseUrl: options.baseUrl,
    waitforTimeout: 6000,
    waitforInterval: 15,
    connectionRetryTimeout: 120000,
    connectionRetryCount: 3,
    services: [],
    framework: 'mocha',
    reporters: reporters,
    mochaOpts: {
        helpers: [],
        timeout: 60000,
    },
};

const wdioConfDir = new URL('wdio.conf.d', import.meta.url)

await Promise.all(fs.readdirSync(wdioConfDir).map(async (file) => {
    const modifierModule = await import(new URL(wdioConfDir +'/' + file, ));
    if (modifierModule.configure) {
        incubatingConfig = modifierModule.configure(incubatingConfig, directories)
    } else {
        throw Error("Please export a function named configure from " + file)
    }
}))

export const config = incubatingConfig
