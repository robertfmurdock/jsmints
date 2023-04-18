import allure from "allure-commandline";
import fs from "fs";
import path from "path";

export function configure(config, directories) {
    const allureDataDir = path.resolve(directories.reports, "../../allure-data");
    if (fs.existsSync(allureDataDir)) {
        fs.rmSync(allureDataDir, {recursive: true})
    }

    config.reporters.push(['allure', {
        outputDir: allureDataDir,
    }]);
    const previousAfterTest = config.afterTest
    config.afterTest = async function (test, context, result) {
        if (previousAfterTest) {
            await previousAfterTest.apply(this, arguments)
        }
        if (result.passed) {
            return;
        }
        await browser.takeScreenshot();
    }
    config.onComplete = function () {
        const generation = allure(
            ['generate', allureDataDir, '--clean', '--output', directories.reports + "allure/report"]
        )
        return new Promise((resolve, reject) => {
            const generationTimeout = setTimeout(
                () => reject(new Error('Could not generate Allure report - timeout')),
                10000)
            generation.on('exit', function (exitCode) {
                clearTimeout(generationTimeout)

                if (exitCode !== 0) {
                    return reject(new Error('Could not generate Allure report - exit code ' + exitCode))
                }

                console.log('Allure report successfully generated')
                resolve()
            })
        })
    }

    return config;
}
