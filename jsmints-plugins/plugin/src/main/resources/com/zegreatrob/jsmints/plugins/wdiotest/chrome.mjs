export function configure(config, directories) {
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
            'args': [
                '--no-sandbox',
                'headless',
                'disable-dev-shm-usage',
                'window-size=800,600',
            ]
        },
    })
    return config
}