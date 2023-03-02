import path from 'path'

export function configure(config, directories) {

    config.afterTest = function (test, context, result) {
        if (result.passed) {
            return;
        }
        const timestamp = new Date().getUTCMilliseconds();
        const filepath = path.join(directories.reports, 'screenshots/', timestamp + '.png');
        browser.saveScreenshot(filepath);
        process.emit('test:screenshot', filepath);
    }

    return config
}
