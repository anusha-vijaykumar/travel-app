# Travel MCP Server

This service exposes the Travel Booking Platform APIs as MCP tools for a chat app or agent client.

## Start

Run the platform services first, including the API gateway on `http://localhost:8083`.

```powershell
cd mcp-server
mvn spring-boot:run
```

If Maven is not installed globally, use one of the existing project wrappers:

```powershell
cd mcp-server
..\booking-service\mvnw.cmd spring-boot:run
```

The MCP server starts on `http://localhost:8091`.

## Chat App Connection

Use Streamable HTTP transport:

```json
{
  "mcpServers": {
    "travel-booking": {
      "url": "http://localhost:8091/mcp"
    }
  }
}
```

If your chat client expects the older SSE transport, enable it in `application.yaml` and point the client to the SSE endpoint supported by the Spring AI MCP server starter.

## Exposed Tools

- `createUser`
- `getUserById`
- `getUserByEmail`
- `createInventory`
- `getInventoryById`
- `getInventoryByTourPackageId`
- `reserveSeats`
- `releaseSeats`
- `createBooking`
- `getBookingById`
- `getBookingsByUserId`
- `cancelBooking`
- `processPayment`
- `getPaymentByBookingId`
- `refundPayment`

Set `travel.api.base-url` if the API gateway runs somewhere other than `http://localhost:8083`.
