const {HtmlReporter, ReportAggregator} = require('@rpii/wdio-html-reporter');
const {WDIOReporter} = require('@wdio/reporter')
const log4js = require('@log4js-node/log4js-api');
const logger = log4js.getLogger('default');

logger.level = "info";

const path = require('path');

const reportDirectory = path.relative('./', process.env.REPORT_DIR) + "/"
const testResultsDir = path.relative('./', process.env.TEST_RESULTS_DIR) + "/"

const config = {
    runner: 'local',
    specs: [
        process.env.SPEC_FILE
    ],
    sync: false,
    exclude: [],
    maxInstances: 1,
    capabilities: [{
        maxInstances: 1,
        browserName: 'chrome',
        "goog:loggingPrefs": {
            "browser": "ALL"
        },
        acceptInsecureCerts: true,
        'goog:chromeOptions': {
            'args': [
                'headless',
                'no-sandbox',
                'show-fps-counter=true',
                'window-size=800,600',
            ]
        },
    }],
    logLevel: 'warn',
    bail: 0,
    baseUrl: `${process.env.BASEURL}`,
    waitforTimeout: 6000,
    waitforInterval: 15, //THIS IS INCREDIBLY IMPORTANT FOR PERFORMANCE
    connectionRetryTimeout: 120000,
    connectionRetryCount: 3,
    services: ['chromedriver'],
    framework: 'mocha',
    reporters: [
        'dot',
        ['junit', {
            outputDir: testResultsDir,
            outputFileFormat: (options) => `results.xml`
        }],
        [HtmlReporter, {
            debug: true,
            outputDir: reportDirectory,
            filename: 'report.html',
            reportTitle: 'Wdio Testing Library E2E Report',
            showInBrowser: true,
            useOnAfterCommandForScreenshot: true,
            LOG: logger
        }
        ],
    ],
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
        let reportAggregator = new ReportAggregator({
            outputDir: reportDirectory,
            filename: 'main-report.html',
            reportTitle: 'Main Report',
            browserName: capabilities.browserName,
        });
        reportAggregator.clean();

        global.reportAggregator = reportAggregator;
    },

    onComplete: async function (exitCode, config, capabilities, results) {
        await global.reportAggregator.createReport();
    },

};

exports.config = config