/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

import * as path from 'path';
import * as os from 'os';

import { Trace } from 'vscode-jsonrpc';
import { commands, window, workspace, ExtensionContext, Uri } from 'vscode';
import { LanguageClient, LanguageClientOptions, ServerOptions } from 'vscode-languageclient/node';

let lc: LanguageClient;

export async function activate(context: ExtensionContext) {
    // The server is a locally installed in src/rosetta
    let launcher = os.platform() === 'win32' ? 'rune-dsl-ls.bat' : 'rune-dsl-ls';
    let script = context.asAbsolutePath(path.join('src', 'rosetta', 'languageserver', 'bin', launcher));
    
    let serverOptions: ServerOptions = {
        run : { command: script, options: { shell: true } },
        debug: { command: script, args: ['-trace'], options: { shell: true, env: createDebugEnv() } },
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
        console.error("Failed to launch Rosetta Language Server.");
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
