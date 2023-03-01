export function configure(config, directories) {
    config.capabilities.push(
        {
            browserName: 'firefox',
            acceptInsecureCerts: true,
            maxInstances: 1,
            'moz:firefoxOptions': {
                args: ['-headless'],
                binary: process.env.FIREFOX_BINARY
            },
        },
        // {
        //     browserName: 'chrome',
        //     "goog:loggingPrefs": {
        //         "browser": "ALL"
        //     },
        //     maxInstances: 1,
        //     acceptInsecureCerts: true,
        //     'goog:chromeOptions': {
        //         'args': [
        //             'no-sandbox',
        //             'headless',
        //             'disable-dev-shm-usage',
        //             'show-fps-counter=true',
        //             'window-size=800,600',
        //         ]
        //     },
        // }
    )

    config.services.push(
        // ['chromedriver', {outputDir: directories.logs}],
        [
            'geckodriver',
            {
                args: ['--log=info'],
                outputDir: directories.logs
            }
        ]
    )

    return config
}
