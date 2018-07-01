# Nervos Docs

[docs.nervos.org]() is the documententation website for all the Nervos Network related components.
This repository is working as both the front page of Nervos Docs and an example for building other Nervos related docs.

## How to Make Another Nervos Dcoument Website in Your Project Repo

### We use Docsify & Github Page
Nervos Documentents are using an easy generated document site framework [Docsify](https://docsify.js.org/#/). It's an awesome project and we really appreciate the the contributors of this project.

Document files are written in Markdown language.

We use github page for hosting our document sites. On the one hand it enables the realtime updating, version control and online editing; on the other hand, their server are much more relible than ours. After all, they are actually Microsoft now.

### How to make the website
Install docsify-cli.
```
yarn global add docsify-cli
```
If you havn't use yarn before, please refer here (https://yarnpkg.com/lang/en/docs/install/).

Get the necessary site files.
```
git clone https://github.com/cryptape/Nervos-Docs.git
```
Do some customizations by configuring the `script/customization-seting.js` you can make some configurations. 

Then run this command to start the document site service.
```
docsify serve
```
Open your browser and enter `http://localhost:3000/` to preview your document site.

### How to Orgnize the Files

#### Necessary Files for Docsify
`index.html` is the only necessary file for starting a Docsify site. All the needed JavaScript configuration files are dynamicly involved from this repo.

`script/` folder has the configuration files.

`.nojekyll` prevents GitHub Pages from ignoring files that begin with an underscore '_'.

`_navbar.md` and `_sidebar.md` are configurations for navigation bar and side bar. Their effective range are their current folder and all the subfolders. They should be inside of the `zh-CN` or `en-US` folder as well as the root folder. The one in the root folder should use the default language (zh-CN in our case).

#### Your Document Files
All the doc files should be along with the project codes and files, inside of the project repo. You can choose either the `repo-name/docs` folder (e.g. `cita/docs`)for storing your docs, or the `gh-pages branch`.

This can make sure two things: every version of codes have the corresponding version of doc; reviewer can know if one changed code without chaging the doc.

The `repo-name/docs` or `gh-pages branch` should be organized in a structure like this:
```
.
├── zh-CN (language)
    ├── V0.0.1 (version)
        ├── first.md
        ├── second.md
        └── third.md
    └── V0.0.2 (version)
        ├── first.md
        ├── second.md
        └── third.md
```

If you want to link to one of the markdown files, the path would be something like: `[First Markdown](/zh-CN/V0.0.1/first)`

#### Sidebar and Navibar Files
You can learn how do it by just simply refering the `_navbar.md` and `_sidebar.md` files in [this repo](https://github.com/cryptape/Nervos-Docs) as an example.

I have faith in you.

### How to put it on Github Page

### Localization (Translation)
