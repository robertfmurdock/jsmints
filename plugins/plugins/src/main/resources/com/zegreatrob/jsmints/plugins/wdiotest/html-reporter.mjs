import log4js from "@log4js-node/log4js-api";
import {HtmlReporter, ReportAggregator} from "wdio-html-nice-reporter"

export function configure(config, directories) {
    const logger = log4js.getLogger('default');
    const outputDir = directories.reports + "/html/";
    config.reporters.push(
        [HtmlReporter, {
            debug: true,
            outputDir: outputDir,
            filename: 'report.html',
            reportTitle: 'Wdio Testing Library E2E Report',
            showInBrowser: true,
            useOnAfterCommandForScreenshot: false,
            linkScreenshots: true,
            LOG: logger
        }]
    )
    let reportAggregator;

    config.onPrepare = function (c, capabilities) {
        reportAggregator = new ReportAggregator({
            outputDir: outputDir,
            filename: 'main-report.html',
            reportTitle: 'Main Report',
            browserName: capabilities.browserName,
        });
        reportAggregator.clean();
    }
    const previousOnComplete = config.onComplete
    config.onComplete = async function () {
        if(previousOnComplete) {
            await previousOnComplete.apply(this, arguments)
        }
        await reportAggregator.createReport();
    }

    return config;
}