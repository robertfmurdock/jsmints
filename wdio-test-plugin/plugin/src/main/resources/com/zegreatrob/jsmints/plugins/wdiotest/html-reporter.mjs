import log4js from "@log4js-node/log4js-api";
import {HtmlReporter, ReportAggregator} from "wdio-html-nice-reporter"

export function configure(config, directories) {
    const logger = log4js.getLogger('default');
    const outputDir = directories.reports + "/html";
    config.reporters.push(
        [HtmlReporter, {
            debug: true,
            outputDir: outputDir,
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
            outputDir: outputDir,
            filename: 'main-report.html',
            reportTitle: 'Main Report',
            browserName: capabilities.browserName,
        });
        reportAggregator.clean();
    }

    config.onComplete = async function () {
        await reportAggregator.createReport();
    }

    return config;
}