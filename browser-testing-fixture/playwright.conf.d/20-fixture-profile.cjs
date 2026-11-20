module.exports = async ({ profile }) => {
    let browserName;
    return {
        contextOptions: {
            viewport: { width: profile === 'firefox' ? 900 : 1000, height: 600 },
        },
        afterLaunch(browser) {
            browserName = browser.browserType().name();
        },
        async afterContext(context) {
            await context.setExtraHTTPHeaders({ 'x-jsmints-browser': browserName });
        },
        async afterPage(page) {
            await page.setExtraHTTPHeaders({ 'x-jsmints-profile': profile });
        },
    };
};
