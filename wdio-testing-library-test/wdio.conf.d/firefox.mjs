export function configure(config, directories) {
    const headless = @HEADLESS@;
    let firefoxArgs = [];
    if(headless) {
        firefoxArgs.push('-headless')
    }
    config.capabilities.push(
        {
            browserName: 'firefox',
            acceptInsecureCerts: true,
            maxInstances: 1,
            'moz:firefoxOptions': {
                args: firefoxArgs,
                binary: process.env.FIREFOX_BINARY
            },
        },
    )

    config.services.push(
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
