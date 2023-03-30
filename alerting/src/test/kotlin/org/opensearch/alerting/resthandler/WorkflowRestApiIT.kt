/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.alerting.resthandler

import org.opensearch.alerting.ALWAYS_RUN
import org.opensearch.alerting.WORKFLOW_ALERTING_BASE_URI
import org.opensearch.alerting.WorkflowRestTestCase
import org.opensearch.alerting.makeRequest
import org.opensearch.alerting.randomBucketLevelMonitor
import org.opensearch.alerting.randomDocumentLevelMonitor
import org.opensearch.alerting.randomDocumentLevelTrigger
import org.opensearch.alerting.randomQueryLevelMonitor
import org.opensearch.alerting.randomWorkflow
import org.opensearch.alerting.randomWorkflowWithDelegates
import org.opensearch.client.ResponseException
import org.opensearch.commons.alerting.model.ChainedFindings
import org.opensearch.commons.alerting.model.CompositeInput
import org.opensearch.commons.alerting.model.Delegate
import org.opensearch.commons.alerting.model.DocLevelMonitorInput
import org.opensearch.commons.alerting.model.DocLevelQuery
import org.opensearch.commons.alerting.model.Monitor
import org.opensearch.commons.alerting.model.Workflow
import org.opensearch.rest.RestStatus
import org.opensearch.test.junit.annotations.TestLogging
import java.util.Collections

@TestLogging("level:DEBUG", reason = "Debug for tests.")
@Suppress("UNCHECKED_CAST")
class WorkflowRestApiIT : WorkflowRestTestCase() {
//
//    fun `test chained alerts`() {
//
//        val docQuery1 = DocLevelQuery(query = "test_field_1:\"us-west-2\"", name = "3")
//        val docLevelInput1 = DocLevelMonitorInput("description", listOf(index), listOf(docQuery1))
//        val trigger1 = randomDocumentLevelTrigger(condition = ALWAYS_RUN)
//        val customAlertsIndex1 = "custom_alerts_index"
//        val customFindingsIndex1 = "custom_findings_index"
//        val customFindingsIndexPattern1 = "custom_findings_index-1"
//        var monitor1 = randomDocumentLevelMonitor(
//            inputs = listOf(docLevelInput1),
//            triggers = listOf(trigger1),
//            dataSources = DataSources(
//                alertsIndex = customAlertsIndex1,
//                findingsIndex = customFindingsIndex1,
//                findingsIndexPattern = customFindingsIndexPattern1
//            )
//        )
//        val monitorResponse = createMonitor(monitor1)!!
//
//        val docQuery2 = DocLevelQuery(query = "source.ip.v6.v2:16645", name = "4")
//        val docLevelInput2 = DocLevelMonitorInput("description", listOf(index), listOf(docQuery2))
//        val trigger2 = randomDocumentLevelTrigger(condition = ALWAYS_RUN)
//        var monitor2 = randomDocumentLevelMonitor(
//            inputs = listOf(docLevelInput2),
//            triggers = listOf(trigger2),
//            dataSources = DataSources(
//                alertsIndex = customAlertsIndex1,
//                findingsIndex = customFindingsIndex1,
//                findingsIndexPattern = customFindingsIndexPattern1
//            )
//        )
//
//        val monitorResponse2 = createMonitor(monitor2)!!
//
//        var workflow = randomWorkflowMonitor(
//            monitorIds = listOf(monitorResponse.id, monitorResponse2.id)
//        )
//        val workflowResponse = upsertWorkflow(workflow)!!
//        val workflowById = searchWorkflow(workflowResponse.id)!!
//        assertNotNull(workflowById)
//
//        var testTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS))
//        // Matches monitor1
//        val testDoc1 = """{
//            "message" : "This is an error from IAD region",
//            "source.ip.v6.v2" : 16644,
//            "test_strict_date_time" : "$testTime",
//            "test_field_1" : "us-west-2"
//        }"""
//        indexDoc(index, "1", testDoc1)
//
//        testTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS))
//        // Matches monitor1 and monitor2
//        val testDoc2 = """{
//            "message" : "This is an error from IAD region",
//            "source.ip.v6.v2" : 16645,
//            "test_strict_date_time" : "$testTime",
//            "test_field_1" : "us-west-2"
//        }"""
//        indexDoc(index, "2", testDoc2)
//
//        testTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS))
//        // Doesn't match
//        val testDoc3 = """{
//            "message" : "This is an error from IAD region",
//            "source.ip.v6.v2" : 16645,
//            "test_strict_date_time" : "$testTime",
//            "test_field_1" : "us-east-1"
//        }"""
//        indexDoc(index, "3", testDoc3)
//
//        val workflowId = workflowResponse.id
//        val executeWorkflowResponse = executeWorkflow(workflowById, workflowId, false)!!
//        val monitorsRunResults = executeWorkflowResponse.workflowRunResult.workflowRunResult
//        assertEquals(3, monitorsRunResults.size)
//
//        assertEquals(monitor1.name, monitorsRunResults[0].monitorName)
//        assertEquals(1, monitorsRunResults[0].triggerResults.size)
//
//        Assert.assertEquals(monitor2.name, monitorsRunResults[1].monitorName)
//        Assert.assertEquals(1, monitorsRunResults[1].triggerResults.size)
//
//        assertAlerts(monitorResponse, customAlertsIndex1, 2)
//        assertFindings(monitorResponse.id, customFindingsIndex1, 2, 2, listOf("1", "2"))
//
//        assertAlerts(monitorResponse2, customAlertsIndex1, 1)
//        assertFindings(monitorResponse2.id, customFindingsIndex1, 1, 1, listOf("2"))
//        assertAlerts((workflowResponse.workflow.inputs.get(0) as CompositeInput).sequence.delegates.get(0).monitorId, customAlertsIndex1, 1)
//    }

    fun `test create workflow success`() {
        logger.error("SASHANK")
        val index = createTestIndex()
        val docQuery1 = DocLevelQuery(query = "source.ip.v6.v1:12345", name = "3")
        val docLevelInput = DocLevelMonitorInput(
            "description", listOf(index), listOf(docQuery1)
        )
        val trigger = randomDocumentLevelTrigger(condition = ALWAYS_RUN)

        val monitor = randomDocumentLevelMonitor(
            inputs = listOf(docLevelInput),
            triggers = listOf(trigger)
        )
        val monitorResponse = createMonitor(monitor)

        val workflow = randomWorkflow(
            monitorIds = listOf(monitorResponse.id)
        )

        val createResponse = client().makeRequest("POST", WORKFLOW_ALERTING_BASE_URI, emptyMap(), workflow.toHttpEntity())

        assertEquals("Create workflow failed", RestStatus.CREATED, createResponse.restStatus())

        val responseBody = createResponse.asMap()
        val createdId = responseBody["_id"] as String
        val createdVersion = responseBody["_version"] as Int

        assertNotEquals("response is missing Id", Workflow.NO_ID, createdId)
        assertTrue("incorrect version", createdVersion > 0)
        assertEquals("Incorrect Location header", "$WORKFLOW_ALERTING_BASE_URI/$createdId", createResponse.getHeader("Location"))
    }

    fun `test create workflow with different monitor types success`() {
        val index = createTestIndex()
        val docQuery = DocLevelQuery(query = "source.ip.v6.v1:12345", name = "3")
        val docLevelInput = DocLevelMonitorInput(
            "description", listOf(index), listOf(docQuery)
        )
        val trigger = randomDocumentLevelTrigger(condition = ALWAYS_RUN)

        val monitor = randomDocumentLevelMonitor(
            inputs = listOf(docLevelInput),
            triggers = listOf(trigger)
        )
        val docLevelMonitorResponse = createMonitor(monitor)

        val bucketLevelMonitor = randomBucketLevelMonitor()
        val bucketLevelMonitorResponse = createMonitor(bucketLevelMonitor)

        val workflow = randomWorkflow(
            monitorIds = listOf(docLevelMonitorResponse.id, bucketLevelMonitorResponse.id)
        )

        val createResponse = client().makeRequest("POST", WORKFLOW_ALERTING_BASE_URI, emptyMap(), workflow.toHttpEntity())

        assertEquals("Create workflow failed", RestStatus.CREATED, createResponse.restStatus())

        val responseBody = createResponse.asMap()
        val createdId = responseBody["_id"] as String
        val createdVersion = responseBody["_version"] as Int

        assertNotEquals("response is missing Id", Workflow.NO_ID, createdId)
        assertTrue("incorrect version", createdVersion > 0)
        assertEquals("Incorrect Location header", "$WORKFLOW_ALERTING_BASE_URI/$createdId", createResponse.getHeader("Location"))

        val workflowById = getWorkflow(createdId)
        assertNotNull(workflowById)

        // Verify workflow
        assertNotEquals("response is missing Id", Monitor.NO_ID, workflowById.id)
        assertTrue("incorrect version", workflowById.version > 0)
        assertEquals("Workflow name not correct", workflow.name, workflowById.name)
        assertEquals("Workflow owner not correct", workflow.owner, workflowById.owner)
        assertEquals("Workflow input not correct", workflow.inputs, workflowById.inputs)

        // Delegate verification
        @Suppress("UNCHECKED_CAST")
        val delegates = (workflowById.inputs as List<CompositeInput>)[0].sequence.delegates.sortedBy { it.order }
        assertEquals("Delegates size not correct", 2, delegates.size)

        val delegate1 = delegates[0]
        assertNotNull(delegate1)
        assertEquals("Delegate1 order not correct", 1, delegate1.order)
        assertEquals("Delegate1 id not correct", docLevelMonitorResponse.id, delegate1.monitorId)

        val delegate2 = delegates[1]
        assertNotNull(delegate2)
        assertEquals("Delegate2 order not correct", 2, delegate2.order)
        assertEquals("Delegate2 id not correct", bucketLevelMonitorResponse.id, delegate2.monitorId)
        assertEquals(
            "Delegate2 Chained finding not correct", docLevelMonitorResponse.id, delegate2.chainedFindings!!.monitorId
        )
    }

    fun `test create workflow without delegate failure`() {
        val workflow = randomWorkflow(
            monitorIds = Collections.emptyList()
        )
        try {
            createWorkflow(workflow)
        } catch (e: ResponseException) {
            assertEquals("Unexpected status", RestStatus.BAD_REQUEST, e.response.restStatus())
            e.message?.let {
                assertTrue(
                    "Exception not returning IndexWorkflow Action error ",
                    it.contains("Delegates list can not be empty.")
                )
            }
        }
    }

    fun `test create workflow duplicate delegate failure`() {
        val workflow = randomWorkflow(
            monitorIds = listOf("1", "1", "2")
        )
        try {
            createWorkflow(workflow)
        } catch (e: ResponseException) {
            assertEquals("Unexpected status", RestStatus.BAD_REQUEST, e.response.restStatus())
            e.message?.let {
                assertTrue(
                    "Exception not returning IndexWorkflow Action error ",
                    it.contains("Duplicate delegates not allowed")
                )
            }
        }
    }

    fun `test create workflow delegate monitor doesn't exist failure`() {
        val index = createTestIndex()
        val docQuery = DocLevelQuery(query = "source.ip.v6.v1:12345", name = "3")
        val docLevelInput = DocLevelMonitorInput(
            "description", listOf(index), listOf(docQuery)
        )
        val trigger = randomDocumentLevelTrigger(condition = ALWAYS_RUN)

        val monitor = randomDocumentLevelMonitor(
            inputs = listOf(docLevelInput),
            triggers = listOf(trigger)
        )
        val docLevelMonitorResponse = createMonitor(monitor)

        val workflow = randomWorkflow(
            monitorIds = listOf("-1", docLevelMonitorResponse.id)
        )
        try {
            createWorkflow(workflow)
        } catch (e: ResponseException) {
            assertEquals("Unexpected status", RestStatus.BAD_REQUEST, e.response.restStatus())
            e.message?.let {
                assertTrue(
                    "Exception not returning IndexWorkflow Action error ",
                    it.contains("are not valid monitor ids")
                )
            }
        }
    }

    fun `test create workflow sequence order not correct failure`() {
        val delegates = listOf(
            Delegate(1, "monitor-1"),
            Delegate(1, "monitor-2"),
            Delegate(2, "monitor-3")
        )
        val workflow = randomWorkflowWithDelegates(
            delegates = delegates
        )
        try {
            createWorkflow(workflow)
        } catch (e: ResponseException) {
            assertEquals("Unexpected status", RestStatus.BAD_REQUEST, e.response.restStatus())
            e.message?.let {
                assertTrue(
                    "Exception not returning IndexWorkflow Action error ",
                    it.contains("Sequence ordering of delegate monitor shouldn't contain duplicate order values")
                )
            }
        }
    }

    fun `test create workflow chained findings monitor not in sequence failure`() {
        val delegates = listOf(
            Delegate(1, "monitor-1"),
            Delegate(2, "monitor-2", ChainedFindings("monitor-1")),
            Delegate(3, "monitor-3", ChainedFindings("monitor-x"))
        )
        val workflow = randomWorkflowWithDelegates(
            delegates = delegates
        )

        try {
            createWorkflow(workflow)
        } catch (e: ResponseException) {
            assertEquals("Unexpected status", RestStatus.BAD_REQUEST, e.response.restStatus())
            e.message?.let {
                assertTrue(
                    "Exception not returning IndexWorkflow Action error ",
                    it.contains("Chained Findings Monitor monitor-x doesn't exist in sequence")
                )
            }
        }
    }

    fun `test create workflow chained findings order not correct failure`() {
        val delegates = listOf(
            Delegate(1, "monitor-1"),
            Delegate(3, "monitor-2", ChainedFindings("monitor-1")),
            Delegate(2, "monitor-3", ChainedFindings("monitor-2"))
        )
        val workflow = randomWorkflowWithDelegates(
            delegates = delegates
        )

        try {
            createWorkflow(workflow)
        } catch (e: ResponseException) {
            assertEquals("Unexpected status", RestStatus.BAD_REQUEST, e.response.restStatus())
            e.message?.let {
                assertTrue(
                    "Exception not returning IndexWorkflow Action error ",
                    it.contains("Chained Findings Monitor monitor-2 should be executed before monitor monitor-3")
                )
            }
        }
    }

    fun `test update workflow add monitor success`() {
        val index = createTestIndex()
        val docQuery1 = DocLevelQuery(query = "source.ip.v6.v1:12345", name = "3")
        val docLevelInput = DocLevelMonitorInput(
            "description", listOf(index), listOf(docQuery1)
        )
        val trigger = randomDocumentLevelTrigger(condition = ALWAYS_RUN)

        val monitor = randomDocumentLevelMonitor(
            inputs = listOf(docLevelInput),
            triggers = listOf(trigger)
        )
        val monitorResponse = createMonitor(monitor)

        val workflow = randomWorkflow(
            monitorIds = listOf(monitorResponse.id)
        )

        val createResponse = client().makeRequest("POST", WORKFLOW_ALERTING_BASE_URI, emptyMap(), workflow.toHttpEntity())

        assertEquals("Create workflow failed", RestStatus.CREATED, createResponse.restStatus())

        val responseBody = createResponse.asMap()
        val createdId = responseBody["_id"] as String
        val createdVersion = responseBody["_version"] as Int

        assertNotEquals("response is missing Id", Workflow.NO_ID, createdId)
        assertTrue("incorrect version", createdVersion > 0)
        assertEquals("Incorrect Location header", "$WORKFLOW_ALERTING_BASE_URI/$createdId", createResponse.getHeader("Location"))

        val monitor2 = randomDocumentLevelMonitor(
            inputs = listOf(docLevelInput),
            triggers = listOf(trigger)
        )

        val monitorResponse2 = createMonitor(monitor2)

        val updatedWorkflow = randomWorkflow(
            id = createdId,
            monitorIds = listOf(monitorResponse.id, monitorResponse2.id)
        )

        val updateResponse = client().makeRequest("PUT", updatedWorkflow.relativeUrl(), emptyMap(), updatedWorkflow.toHttpEntity())

        assertEquals("Update workflow failed", RestStatus.OK, updateResponse.restStatus())

        val updateResponseBody = updateResponse.asMap()
        val updatedId = updateResponseBody["_id"] as String
        val updatedVersion = updateResponseBody["_version"] as Int

        assertNotEquals("response is missing Id", Workflow.NO_ID, updatedId)
        assertTrue("incorrect version", updatedVersion > 0)

        val workflowById = getWorkflow(updatedId)
        assertNotNull(workflowById)
        // Delegate verification
        @Suppress("UNCHECKED_CAST")
        val delegates = (workflowById.inputs as List<CompositeInput>)[0].sequence.delegates.sortedBy { it.order }
        assertEquals("Delegates size not correct", 2, delegates.size)

        val delegate1 = delegates[0]
        assertNotNull(delegate1)
        assertEquals("Delegate1 order not correct", 1, delegate1.order)
        assertEquals("Delegate1 id not correct", monitorResponse.id, delegate1.monitorId)

        val delegate2 = delegates[1]
        assertNotNull(delegate2)
        assertEquals("Delegate2 order not correct", 2, delegate2.order)
        assertEquals("Delegate2 id not correct", monitorResponse2.id, delegate2.monitorId)
        assertEquals(
            "Delegate2 Chained finding not correct", monitorResponse.id, delegate2.chainedFindings!!.monitorId
        )
    }

    fun `test update workflow remove monitor success`() {
        val index = createTestIndex()
        val docQuery1 = DocLevelQuery(query = "source.ip.v6.v1:12345", name = "3")
        val docLevelInput = DocLevelMonitorInput(
            "description", listOf(index), listOf(docQuery1)
        )
        val trigger = randomDocumentLevelTrigger(condition = ALWAYS_RUN)

        val monitor = randomDocumentLevelMonitor(
            inputs = listOf(docLevelInput),
            triggers = listOf(trigger)
        )
        val monitorResponse = createMonitor(monitor)

        val monitor2 = randomDocumentLevelMonitor(
            inputs = listOf(docLevelInput),
            triggers = listOf(trigger)
        )

        val monitorResponse2 = createMonitor(monitor2)

        val workflow = randomWorkflow(
            monitorIds = listOf(monitorResponse.id, monitorResponse2.id)
        )

        val createResponse = client().makeRequest("POST", WORKFLOW_ALERTING_BASE_URI, emptyMap(), workflow.toHttpEntity())

        assertEquals("Create workflow failed", RestStatus.CREATED, createResponse.restStatus())

        val responseBody = createResponse.asMap()
        val createdId = responseBody["_id"] as String
        val createdVersion = responseBody["_version"] as Int

        assertNotEquals("response is missing Id", Workflow.NO_ID, createdId)
        assertTrue("incorrect version", createdVersion > 0)
        assertEquals("Incorrect Location header", "$WORKFLOW_ALERTING_BASE_URI/$createdId", createResponse.getHeader("Location"))

        var workflowById = getWorkflow(createdId)
        assertNotNull(workflowById)
        // Delegate verification
        @Suppress("UNCHECKED_CAST")
        var delegates = (workflowById.inputs as List<CompositeInput>)[0].sequence.delegates.sortedBy { it.order }
        assertEquals("Delegates size not correct", 2, delegates.size)

        val updatedWorkflow = randomWorkflow(
            id = createdId,
            monitorIds = listOf(monitorResponse.id)
        )

        val updateResponse = client().makeRequest("PUT", updatedWorkflow.relativeUrl(), emptyMap(), updatedWorkflow.toHttpEntity())

        assertEquals("Update workflow failed", RestStatus.OK, updateResponse.restStatus())

        val updateResponseBody = updateResponse.asMap()
        val updatedId = updateResponseBody["_id"] as String
        val updatedVersion = updateResponseBody["_version"] as Int

        assertNotEquals("response is missing Id", Workflow.NO_ID, updatedId)
        assertTrue("incorrect version", updatedVersion > 0)

        workflowById = getWorkflow(updatedId)
        assertNotNull(workflowById)
        // Delegate verification
        @Suppress("UNCHECKED_CAST")
        delegates = (workflowById.inputs as List<CompositeInput>)[0].sequence.delegates.sortedBy { it.order }
        assertEquals("Delegates size not correct", 1, delegates.size)

        val delegate1 = delegates[0]
        assertNotNull(delegate1)
        assertEquals("Delegate1 order not correct", 1, delegate1.order)
        assertEquals("Delegate1 id not correct", monitorResponse.id, delegate1.monitorId)
    }

    @Throws(Exception::class)
    fun `test getting a workflow`() {
        val query = randomQueryLevelMonitor()
        val monitor = createMonitor(query)
        val storedMonitor = getMonitor(monitor.id)

        assertEquals("Indexed and retrieved monitor differ", monitor, storedMonitor)

        val workflow = createRandomWorkflow(monitorIds = listOf(monitor.id))

        val storedWorkflow = getWorkflow(workflow.id)

        assertEquals("Indexed and retrieved workflow differ", workflow.id, storedWorkflow.id)
        val delegates = (storedWorkflow.inputs[0] as CompositeInput).sequence.delegates
        assertEquals("Delegate list not correct", 1, delegates.size)
        assertEquals("Delegate order id not correct", 1, delegates[0].order)
        assertEquals("Delegate id list not correct", monitor.id, delegates[0].monitorId)
    }

    @Throws(Exception::class)
    fun `test getting a workflow that doesn't exist`() {
        try {
            getWorkflow(randomAlphaOfLength(20))
            fail("expected response exception")
        } catch (e: ResponseException) {
            assertEquals(RestStatus.NOT_FOUND, e.response.restStatus())
        }
    }

    @Throws(Exception::class)
    fun `test checking if a workflow exists`() {
        val query = randomQueryLevelMonitor()
        val monitor = createMonitor(query)

        // val monitor = createMonitor(docLevelMonitor)
        val storedMonitor = getMonitor(monitor.id)
        assertEquals("Indexed and retrieved monitor differ", monitor, storedMonitor)
        val workflow = createRandomWorkflow(monitorIds = listOf(monitor.id))

        val headResponse = client().makeRequest("HEAD", workflow.relativeUrl())
        assertEquals("Unable to HEAD workflow", RestStatus.OK, headResponse.restStatus())
        assertNull("Workflow response contains unexpected body", headResponse.entity)
    }

    fun `test checking if a non-existent workflow exists`() {
        val headResponse = client().makeRequest("HEAD", "$WORKFLOW_ALERTING_BASE_URI/foobarbaz")
        assertEquals("Unexpected status", RestStatus.NOT_FOUND, headResponse.restStatus())
    }

    fun `test delete workflow`() {
        val query = randomQueryLevelMonitor()
        val monitor = createMonitor(query)

        val workflowRequest = randomWorkflow(
            monitorIds = listOf(monitor.id)
        )
        val workflowResponse = createWorkflow(workflowRequest)
        val workflowId = workflowResponse.id
        val getWorkflowResponse = getWorkflow(workflowResponse.id)

        assertNotNull(getWorkflowResponse)
        assertEquals(workflowId, getWorkflowResponse.id)

        client().makeRequest("DELETE", getWorkflowResponse.relativeUrl())

        // Verify that the workflow is deleted
        try {
            getWorkflow(workflowId)
        } catch (e: ResponseException) {
            assertEquals(RestStatus.NOT_FOUND, e.response.restStatus())
            e.message?.let {
                assertTrue(
                    "Exception not returning GetWorkflow Action error ",
                    it.contains("Workflow not found.")
                )
            }
        }
    }

    fun `test delete workflow delete delegate monitors`() {
        val query = randomQueryLevelMonitor()
        val monitor = createMonitor(query)

        val workflowRequest = randomWorkflow(
            monitorIds = listOf(monitor.id)
        )
        val workflowResponse = createWorkflow(workflowRequest)
        val workflowId = workflowResponse.id
        val getWorkflowResponse = getWorkflow(workflowResponse.id)

        assertNotNull(getWorkflowResponse)
        assertEquals(workflowId, getWorkflowResponse.id)

        client().makeRequest("DELETE", getWorkflowResponse.relativeUrl().plus("?deleteDelegateMonitors=true"))

        // Verify that the workflow is deleted
        try {
            getWorkflow(workflowId)
        } catch (e: ResponseException) {
            assertEquals(RestStatus.NOT_FOUND, e.response.restStatus())
            e.message?.let {
                assertTrue(
                    "Exception not returning GetWorkflow Action error ",
                    it.contains("Workflow not found.")
                )
            }
        }

        // Verify that delegate monitor is deleted
        try {
            getMonitor(monitor.id)
        } catch (e: ResponseException) {
            assertEquals(RestStatus.NOT_FOUND, e.response.restStatus())
            e.message?.let {
                assertTrue(
                    "Exception not returning GetWorkflow Action error ",
                    it.contains("Monitor not found.")
                )
            }
        }
    }

    fun `test delete workflow preserve delegate monitors`() {
        val query = randomQueryLevelMonitor()
        val monitor = createMonitor(query)

        val workflowRequest = randomWorkflow(
            monitorIds = listOf(monitor.id)
        )
        val workflowResponse = createWorkflow(workflowRequest)
        val workflowId = workflowResponse.id
        val getWorkflowResponse = getWorkflow(workflowResponse.id)

        assertNotNull(getWorkflowResponse)
        assertEquals(workflowId, getWorkflowResponse.id)

        client().makeRequest("DELETE", getWorkflowResponse.relativeUrl().plus("?deleteDelegateMonitors=false"))

        // Verify that the workflow is deleted
        try {
            getWorkflow(workflowId)
        } catch (e: ResponseException) {
            assertEquals(RestStatus.NOT_FOUND, e.response.restStatus())
            e.message?.let {
                assertTrue(
                    "Exception not returning GetWorkflow Action error ",
                    it.contains("Workflow not found.")
                )
            }
        }

        // Verify that delegate monitor is not deleted
        val delegateMonitor = getMonitor(monitor.id)
        assertNotNull(delegateMonitor)
    }

    @Throws(Exception::class)
    fun `test deleting a workflow that doesn't exist`() {
        try {
            client().makeRequest("DELETE", "$WORKFLOW_ALERTING_BASE_URI/foobarbaz")
            fail("expected 404 ResponseException")
        } catch (e: ResponseException) {
            assertEquals(RestStatus.NOT_FOUND, e.response.restStatus())
        }
    }
}
