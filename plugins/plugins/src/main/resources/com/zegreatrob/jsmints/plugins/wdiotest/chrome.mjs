export function configure(config, directories) {
    const chromeBinary = "@CHROME_BINARY@";
    const headless = @HEADLESS@;
    let chromeArgs = [];
    if(headless){
        chromeArgs.push('headless')
    }
    let chromeOptions = {
        'args': chromeArgs
    };
    if(chromeBinary) {
        chromeOptions.binary = chromeBinary
    }
    config.capabilities.push({
        maxInstances: 1,
        acceptInsecureCerts: true,
        browserName: 'chrome',
        browserVersion: 'stable',
        "goog:loggingPrefs": {
            "browser": "ALL"
        },
        'goog:chromeOptions': chromeOptions,
    })
    return config
}