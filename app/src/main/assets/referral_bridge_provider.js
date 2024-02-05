
// we need this JS code to inject the ReferralBridge object into the global namespace
// and parse the result data into a JSON object instead of raw string
window.ReferralBridge = {
    getUserPublicKeyAsync: async function() {
        const result = await AndroidReferralBridge.getUserPublicKeyAsync();
        return JSON.parse(result)
    },
    nativeLog: function(info) {
        AndroidReferralBridge.nativeLog(info);
    },
    showShareDialog: function(link) {
        AndroidReferralBridge.showShareDialog(link);
    },
    signMessageAsync: async function(message) {
        const result = AndroidReferralBridge.signMessageAsync(message);
        return JSON.parse(result)
    }
}
