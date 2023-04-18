import TimelineService from 'wdio-timeline-reporter/timeline-service.js'

export function configure(config, directories) {
    const outputDir = directories.reports + "/timeline";
    config.reporters.push(['timeline', {
        outputDir: outputDir,
    }]);
    config.services.push([TimelineService])
    return config;
}
