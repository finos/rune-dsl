'use strict';

import * as path from 'path';
import * as os from 'os';

import { Trace } from 'vscode-jsonrpc';
import { commands, window, workspace, ExtensionContext, Uri } from 'vscode';
import { LanguageClient, LanguageClientOptions, ServerOptions } from 'vscode-languageclient/node';

let lc: LanguageClient;

export async function activate(context: ExtensionContext) {
    // The server is a locally installed in src/rosetta
    let launcher = os.platform() === 'win32' ? 'rosetta-dsl-ls.bat' : 'rosetta-dsl-ls';
    let script = context.asAbsolutePath(path.join('src', 'rosetta', 'bin', launcher));
    
    let serverOptions: ServerOptions = {
        run : { command: script },
        debug: { command: script, args: ['-trace'], options: { env: createDebugEnv() } }
    };
    
    let clientOptions: LanguageClientOptions = {
        documentSelector: ['rosetta'],
        synchronize: {
            fileEvents: workspace.createFileSystemWatcher('**/*.*')
        }
    };
    
    // Create the language client and start the client.
    lc = new LanguageClient('Rosetta Language Server', serverOptions, clientOptions);
    
    var disposable2 = commands.registerCommand("rosetta.a.proxy", async () => {
        let activeEditor = window.activeTextEditor;
        if (!activeEditor || !activeEditor.document || activeEditor.document.languageId !== 'rosetta') {
            return;
        }

        if (activeEditor.document.uri instanceof Uri) {
            commands.executeCommand("rosetta.a", activeEditor.document.uri.toString());
        }
    })
    context.subscriptions.push(disposable2);
    
    // enable tracing (.Off, .Messages, Verbose)
    lc.setTrace(Trace.Verbose);
    console.log("Launching Rosetta Language Server...");
    await lc.start().then(() => {
        console.log("Rosetta Language Server started.");
    }).catch((err) => {
        console.error("Failed to launch Rosetta Language Server.")
        console.error(err);
    });
}
export function deactivate() {
    console.log("Stopping Rosetta Language Server...");
    return lc.stop();
}
function createDebugEnv() {
    return Object.assign({
        JAVA_OPTS:"-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n,quiet=y"
    }, process.env)
}
