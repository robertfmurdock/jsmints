import path from "path";
import fs from "fs";

const reportDirectory = path.relative('./', process.env.REPORT_DIR) + "/"
const testResultsDir = path.relative('./', process.env.TEST_RESULTS_DIR) + "/"
const logDir = path.relative('./', process.env.LOGS_DIR) + "/"

const options = {
    enableHtmlReporter: @ENABLE_HTML_REPORTER@,
    useChrome: @USE_CHROME@,
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
    baseUrl: `${process.env.BASEURL}`,
    waitforTimeout: 6000,
    waitforInterval: 15, //THIS IS INCREDIBLY IMPORTANT FOR PERFORMANCE
    connectionRetryTimeout: 120000,
    connectionRetryCount: 3,
    services: [],
    framework: 'mocha',
    reporters: reporters,
    mochaOpts: {
        helpers: [],
        timeout: 60000,
    },
    beforeSession: async function () {
    },

    afterTest: function (test, context, result) {
        if (result.passed) {
            return;
        }
        const timestamp = new Date().getUTCMilliseconds();
        const filepath = path.join(reportDirectory, 'screenshots/', timestamp + '.png');
        browser.saveScreenshot(filepath);
        process.emit('test:screenshot', filepath);
    },
    onPrepare: function (config, capabilities) {
    },

};

if (options.useChrome) {
    incubatingConfig.services.push(
        ['chromedriver', {outputDir: logDir}],
    )
    incubatingConfig.capabilities.push({
        maxInstances: 1,
        acceptInsecureCerts: true,
        browserName: 'chrome',
        "goog:loggingPrefs": {
            "browser": "ALL"
        },
        'goog:chromeOptions': {
            'args': [
                'headless',
                'show-fps-counter=true',
            ]
        },
    })
}

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
