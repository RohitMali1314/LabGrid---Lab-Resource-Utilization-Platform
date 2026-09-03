declare module "sockjs-client/dist/sockjs.js" {
  const SockJS: new (url: string, protocols?: string[], options?: unknown) => WebSocket;
  export default SockJS;
}
