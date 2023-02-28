import log4js from "@log4js-node/log4js-api";
import path from "path";

const logger = log4js.getLogger('default');
logger.level = "info";
const reportDirectory = path.relative('./', process.env.REPORT_DIR) + "/"
const testResultsDir = path.relative('./', process.env.TEST_RESULTS_DIR) + "/"
const logDir = path.relative('./', process.env.LOGS_DIR) + "/"

const options = {
    enableHtmlReporter: @ENABLE_HTML_REPORTER@,
    useChrome: @USE_CHROME@,
}

const reporters = [
    'dot',
    ['junit', {
        outputDir: testResultsDir,
        outputFileFormat: (options) => `results.xml`
    }],
];

export const config = {
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
        console.log("onPrepare old")
    },

};

if (options.useChrome) {
    config.services.push(
        ['chromedriver', {outputDir: logDir}],
    )
    config.capabilities.push({
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

if (options.enableHtmlReporter) {
    const {HtmlReporter, ReportAggregator} = await import("wdio-html-nice-reporter")

    reporters.push(
        [HtmlReporter, {
            debug: true,
            outputDir: reportDirectory,
            filename: 'report.html',
            reportTitle: 'Wdio Testing Library E2E Report',
            showInBrowser: true,
            useOnAfterCommandForScreenshot: true,
            LOG: logger
        }]
    )
    let reportAggregator;

    const oldPrepare = config.onPrepare
    config.onPrepare = function (c, capabilities) {
        oldPrepare(c, capabilities)
        reportAggregator = new ReportAggregator({
            outputDir: reportDirectory,
            filename: 'main-report.html',
            reportTitle: 'Main Report',
            browserName: capabilities.browserName,
        });
        reportAggregator.clean();
    }

    config.onComplete = async function (exitCode, config, capabilities, results) {
        await reportAggregator.createReport();
    }

}