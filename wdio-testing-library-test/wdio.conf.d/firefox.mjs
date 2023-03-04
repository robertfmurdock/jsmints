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
