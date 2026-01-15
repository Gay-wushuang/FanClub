-- CreateEnum
CREATE TYPE "Platform" AS ENUM ('BILIBILI');

-- CreateEnum
CREATE TYPE "EventType" AS ENUM ('GIFT', 'SUPERCHAT', 'GUARD', 'ENTER', 'DANMU');

-- CreateTable
CREATE TABLE "Creator" (
    "id" TEXT NOT NULL,
    "name" TEXT,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "Creator_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Room" (
    "id" TEXT NOT NULL,
    "platform" "Platform" NOT NULL,
    "platformRoomId" TEXT NOT NULL,
    "creatorId" TEXT NOT NULL,
    "isEnabled" BOOLEAN NOT NULL DEFAULT true,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "Room_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Fanclub" (
    "id" TEXT NOT NULL,
    "creatorId" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "Fanclub_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "RawEvent" (
    "id" TEXT NOT NULL,
    "platform" "Platform" NOT NULL,
    "roomId" TEXT NOT NULL,
    "receivedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "payload" JSONB NOT NULL,
    "traceId" TEXT NOT NULL,

    CONSTRAINT "RawEvent_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "NormalizedEvent" (
    "id" TEXT NOT NULL,
    "platform" "Platform" NOT NULL,
    "roomId" TEXT NOT NULL,
    "eventType" "EventType" NOT NULL,
    "occurredAt" TIMESTAMP(3) NOT NULL,
    "receivedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "idempotencyKey" TEXT NOT NULL,
    "platformEventId" TEXT,
    "actorUid" TEXT,
    "giftId" TEXT,
    "giftCount" INTEGER,
    "amount" DECIMAL(18,6),
    "rawEventId" TEXT,

    CONSTRAINT "NormalizedEvent_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "LedgerBEntry" (
    "id" TEXT NOT NULL,
    "roomId" TEXT NOT NULL,
    "creatorId" TEXT NOT NULL,
    "fanUid" TEXT,
    "delta" INTEGER NOT NULL,
    "reason" TEXT NOT NULL,
    "normalizedEventId" TEXT NOT NULL,
    "occurredAt" TIMESTAMP(3) NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "LedgerBEntry_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE INDEX "Room_creatorId_idx" ON "Room"("creatorId");

-- CreateIndex
CREATE UNIQUE INDEX "Room_platform_platformRoomId_key" ON "Room"("platform", "platformRoomId");

-- CreateIndex
CREATE INDEX "Fanclub_creatorId_idx" ON "Fanclub"("creatorId");

-- CreateIndex
CREATE INDEX "RawEvent_platform_roomId_receivedAt_idx" ON "RawEvent"("platform", "roomId", "receivedAt");

-- CreateIndex
CREATE INDEX "NormalizedEvent_roomId_occurredAt_idx" ON "NormalizedEvent"("roomId", "occurredAt");

-- CreateIndex
CREATE UNIQUE INDEX "NormalizedEvent_platform_idempotencyKey_key" ON "NormalizedEvent"("platform", "idempotencyKey");

-- CreateIndex
CREATE UNIQUE INDEX "LedgerBEntry_normalizedEventId_key" ON "LedgerBEntry"("normalizedEventId");

-- CreateIndex
CREATE INDEX "LedgerBEntry_creatorId_occurredAt_idx" ON "LedgerBEntry"("creatorId", "occurredAt");

-- CreateIndex
CREATE INDEX "LedgerBEntry_roomId_occurredAt_idx" ON "LedgerBEntry"("roomId", "occurredAt");

-- CreateIndex
CREATE INDEX "LedgerBEntry_fanUid_occurredAt_idx" ON "LedgerBEntry"("fanUid", "occurredAt");

-- AddForeignKey
ALTER TABLE "Room" ADD CONSTRAINT "Room_creatorId_fkey" FOREIGN KEY ("creatorId") REFERENCES "Creator"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Fanclub" ADD CONSTRAINT "Fanclub_creatorId_fkey" FOREIGN KEY ("creatorId") REFERENCES "Creator"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "RawEvent" ADD CONSTRAINT "RawEvent_roomId_fkey" FOREIGN KEY ("roomId") REFERENCES "Room"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "NormalizedEvent" ADD CONSTRAINT "NormalizedEvent_rawEventId_fkey" FOREIGN KEY ("rawEventId") REFERENCES "RawEvent"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "NormalizedEvent" ADD CONSTRAINT "NormalizedEvent_roomId_fkey" FOREIGN KEY ("roomId") REFERENCES "Room"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "LedgerBEntry" ADD CONSTRAINT "LedgerBEntry_roomId_fkey" FOREIGN KEY ("roomId") REFERENCES "Room"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "LedgerBEntry" ADD CONSTRAINT "LedgerBEntry_creatorId_fkey" FOREIGN KEY ("creatorId") REFERENCES "Creator"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "LedgerBEntry" ADD CONSTRAINT "LedgerBEntry_normalizedEventId_fkey" FOREIGN KEY ("normalizedEventId") REFERENCES "NormalizedEvent"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
