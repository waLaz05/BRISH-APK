// SERVICE WORKER KILLED
// This file effectively unregisters itself immediately.
self.addEventListener('install', () => {
    self.skipWaiting();
});

self.addEventListener('activate', () => {
    self.registration.unregister()
        .then(() => console.log('SW Unregistered itself'))
        .catch((e) => console.log('SW Unregister failed', e));
});
