export function configure(config, directories) {
    const chromeBinary = "@CHROME_BINARY@";
    const headless = @HEADLESS@;
    let chromeArgs = [];
    if(headless){
        chromeArgs.push('headless')
    }
    config.capabilities.push({
        maxInstances: 1,
        acceptInsecureCerts: true,
        browserName: 'chrome',
        browserVersion: 'stable',
        "goog:loggingPrefs": {
            "browser": "ALL"
        },
        'goog:chromeOptions': {
            'binary': chromeBinary,
            'args': chromeArgs
        },
    })
    return config
}