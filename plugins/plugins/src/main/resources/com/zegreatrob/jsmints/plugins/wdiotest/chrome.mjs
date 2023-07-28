export function configure(config, directories) {
    const chromeBinary = "@CHROME_BINARY@";
    config.services.push(
        ['chromedriver', {outputDir: directories.logs}],
    )
    config.capabilities.push({
        maxInstances: 1,
        acceptInsecureCerts: true,
        browserName: 'chrome',
        "goog:loggingPrefs": {
            "browser": "ALL"
        },
        'goog:chromeOptions': {
            'binary': chromeBinary,
            'args': [
                'headless',
            ]
        },
    })
    return config
}