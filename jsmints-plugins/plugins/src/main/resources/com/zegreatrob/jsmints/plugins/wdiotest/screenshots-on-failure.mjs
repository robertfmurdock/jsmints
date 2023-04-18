import path from 'path'

export function configure(config, directories) {
    const previousAfterTest = config.afterTest
    config.afterTest = async function (test, context, result) {
        if (previousAfterTest) {
            await previousAfterTest.apply(this, arguments)
        }
        if (result.passed) {
            return;
        }
        const timestamp = new Date().getUTCMilliseconds();
        const filepath = path.join(directories.reports, 'html/screenshots/', timestamp + '.png');
        browser.saveScreenshot(filepath);
        process.emit('test:screenshot', filepath);
    }

    return config
}
