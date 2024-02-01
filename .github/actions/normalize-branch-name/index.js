const core = require("@actions/core");
try {
    const input = core.getInput("branch") ? core.getInput("branch") : process.env.GITHUB_HEAD_REF ? process.env.GITHUB_HEAD_REF : process.env.GITHUB_REF ? process.env.GITHUB_REF.replace("refs/heads/", "") : null;
    if (!input) {
        throw new Error("No branch was found.");
    }
    let output = input
        .trim()
        .toLowerCase()
        .replace(/([^0-9a-zA-Z-]+)/g, "-");
    core.setOutput("normalized", output);
    if (output.charAt(output.length - 1) == '-') {
        output = output.substring(0, output.length - 1);
    }
} catch (err) {
    core.setFailed(err);
}