// this file is NOT for customization
// these are the default settings for all the Nervos Documents
// you can overwrite settings in this page by set them again in customization.js

var common = {

    loadSidebar: true,
    autoHeader: true,
    subMaxLevel: 2,
    loadNavbar: true,
    basePath: './',

    // search: 'auto',

    // the default 
    alias: {
        '/_sidebar.md': `/${default_language}/_sidebar.md`,
        '/_navbar.md': `/${default_language}/_navbar.md`,
    },

    // configuration for searching plugin
    search: {
        maxAge: 86400000, // expiration time in milliseconds, one day by default
        // paths: [
        // '/',
        // ], // or 'auto'

        // localization
        placeholder: {
            '/zh-CN/': '搜索',
            '/': 'Type to search',
        },


        // localization
        noData: {
            '/zh-CN/': '找不到结果',
            '/': 'No Results',
        },

        // depth of the maximum searching title levels
        depth: 6,
    },

        // add edit on github feature
        plugins: [
            function(hook, vm) {
              hook.afterEach(function (html,next) {
                var url = github_url + vm.route.file
                var editHtml = `<hr> If you find any mistakes on this page, feel free to <a target='_blank' href="${url}">edit this document on GitHub</a>`
        
                next( html + editHtml)
              })
            }
          ]


}