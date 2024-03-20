import com.stadiamaps.api.apis.RoutingApi
import com.stadiamaps.api.auth.ApiKeyAuth
import com.stadiamaps.api.infrastructure.ApiClient
import com.stadiamaps.api.models.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

internal class TestRoutingApi {
    private val apiKey = System.getenv("STADIA_API_KEY") ?: throw RuntimeException("API Key not set")
    private lateinit var service: RoutingApi
    private val costingOptions =
        CostingOptions(auto = AutoCostingOptions(useHighways = 0.3))  // Take the scenic route ;)

    private val locationA = Coordinate(40.042072, -76.306572)
    private val locationB = Coordinate(39.992115, -76.781559)
    private val locationC = Coordinate(39.984519, -76.6956)

    @BeforeEach
    fun setUp() {
        val client = ApiClient()
        client.addAuthorization("ApiKeyAuth", ApiKeyAuth("query", "api_key", apiKey))
        service = client.createService(RoutingApi::class.java)
    }

    @Test
    fun testRoute() {
        val req = RouteRequest(
            id = "route",
            locations = listOf(
                RoutingWaypoint(locationA.lat, locationA.lon), RoutingWaypoint(locationB.lat, locationB.lon)
            ),
            costing = CostingModel.auto,
            costingOptions = costingOptions,
            units = DistanceUnit.mi,
            language = ValhallaLanguages.enMinusGB
        )
        val res = service.route(req).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals(req.id, body.id)
        assertEquals(0, body.trip.status)
        assertEquals(ValhallaLongUnits.miles, body.trip.units)
        assertEquals(1, body.trip.legs.count())
        assertEquals(0, body.alternates?.count() ?: 0)
    }

    @Test
    fun testRouteWithAlternates() {
        val req = RouteRequest(
            id = "route",
            locations = listOf(
                RoutingWaypoint(locationA.lat, locationA.lon), RoutingWaypoint(locationB.lat, locationB.lon)
            ),
            costing = CostingModel.auto,
            costingOptions = costingOptions,
            units = DistanceUnit.mi,
            language = ValhallaLanguages.enMinusGB,
            alternates = 1
        )
        val res = service.route(req).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals(req.id, body.id)
        assertEquals(0, body.trip.status)
        assertEquals(ValhallaLongUnits.miles, body.trip.units)
        assertEquals(1, body.trip.legs.count())
        assertEquals(1, body.alternates?.count() ?: 0)
    }

    @Test
    fun testHybridBicycleRoute() {
        val req = RouteRequest(
            id = "route",
            locations = listOf(
                RoutingWaypoint(locationA.lat, locationA.lon), RoutingWaypoint(locationB.lat, locationB.lon)
            ),
            costing = CostingModel.bicycle,
            costingOptions = CostingOptions(
                bicycle = BicycleCostingOptions(
                    bicycleType = BicycleCostingOptions.BicycleType.hybrid,
                    useRoads = 0.4,
                    useHills = 0.6
                )
            ),
            units = DistanceUnit.mi,
            language = ValhallaLanguages.enMinusGB
        )
        val res = service.route(req).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals(req.id, body.id)
        assertEquals(0, body.trip.status)
        assertEquals(ValhallaLongUnits.miles, body.trip.units)
        assertEquals(1, body.trip.legs.count())
    }

    @Test
    fun testOptimizedRoute() {
        val req = OptimizedRouteRequest(
            id = "optimized_route",
            locations = listOf(locationA, locationB, locationC, locationA),
            costing = MatrixCostingModel.auto,
            costingOptions = costingOptions,
            units = DistanceUnit.mi,
            language = ValhallaLanguages.enMinusGB
        )
        val res = service.optimizedRoute(req).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals(req.id, body.id)
        assertEquals(0, body.trip.status)
        assertEquals(ValhallaLongUnits.miles, body.trip.units)
        assertTrue(body.trip.legs.count() > 1)
    }

    @Test
    fun testTimeDistanceMatrix() {
        val req = MatrixRequest(
            id = "matrix",
            sources = listOf(locationA),
            targets = listOf(locationB, locationC),
            costing = MatrixCostingModel.auto,
            costingOptions = costingOptions,
        )
        val res = service.timeDistanceMatrix(req).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals(req.id, body.id)
        assertEquals(req.sources.count(), body.sources.count())
        assertEquals(req.targets.count(), body.targets.count())
        assertEquals(ValhallaLongUnits.kilometers, body.units)
        assertTrue(body.sourcesToTargets.isNotEmpty())
    }

    @Test
    fun testNearestRoads() {
        val req = NearestRoadsRequest(locations = listOf(locationA, locationB, locationC))
        val res = service.nearestRoads(req).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals(3, body.count())
        assertTrue(body.first().edges!!.isNotEmpty())
    }

    @Test
    fun testIsochrone() {
        val req = IsochroneRequest(
            id = "isochrone",
            locations = listOf(locationA),
            costing = IsochroneCostingModel.pedestrian,
            contours = listOf(Contour(time = 5.0, color = "aabbcc")),
            polygons = true,
        )
        val res = service.isochrone(req).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals(req.id, body.id)
        assertTrue(body.features.isNotEmpty())
        assertEquals(IsochroneResponse.Type.featureCollection, body.type)
    }

    @Test
    fun testMapMatch() {
        val req = MapMatchRequest(
            id = "map_match",
            encodedPolyline = "_grbgAh~{nhF?lBAzBFvBHxBEtBKdB?fB@dBZdBb@hBh@jBb@x@\\|@x@pB\\x@v@hBl@nBPbCXtBn@|@z@ZbAEbAa@~@q@z@QhA]pAUpAVhAPlAWtASpAAdA[dASdAQhAIlARjANnAZhAf@n@`A?lB^nCRbA\\xB`@vBf@tBTbCFbARzBZvBThBRnBNrBP`CHbCF`CNdCb@vBX`ARlAJfADhA@dAFdAP`AR`Ah@hBd@bBl@rBV|B?vB]tBCvBBhAF`CFnBXtAVxAVpAVtAb@|AZ`Bd@~BJfA@fAHdADhADhABjAGzAInAAjAB|BNbCR|BTjBZtB`@lBh@lB\\|Bl@rBXtBN`Al@g@t@?nAA~AKvACvAAlAMdAU`Ac@hAShAI`AJ`AIdAi@bAu@|@k@p@]p@a@bAc@z@g@~@Ot@Bz@f@X`BFtBXdCLbAf@zBh@fBb@xAb@nATjAKjAW`BI|AEpAHjAPdAAfAGdAFjAv@p@XlAVnA?~A?jAInAPtAVxAXnAf@tBDpBJpBXhBJfBDpAZ|Ax@pAz@h@~@lA|@bAnAd@hAj@tAR~AKxAc@xAShA]hAIdAAjA]~A[v@BhB?dBSv@Ct@CvAI~@Oz@Pv@dAz@lAj@~A^`B^|AXvAVpAXdBh@~Ap@fCh@hB\\zBN`Aj@xBFdA@jALbAPbAJdAHdAJbAHbAHfAJhALbA\\lBTvBAdC@bC@jCKjASbC?`CM`CDpB\\xAj@tB\\fA\\bAVfAJdAJbAXz@L|BO`AOdCDdA@~B\\z@l@v@l@v@l@r@j@t@b@x@b@r@z@jBVfCJdAJdANbCPfCF|BRhBS~BS`AYbAe@~BQdA",
            costing = MapMatchCostingModel.pedestrian,
            units = DistanceUnit.mi,
            language = ValhallaLanguages.enMinusGB,
            linearReferences = true,
        )
        val res = service.mapMatch(req).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals(req.id, body.id)
        assertEquals(0, body.trip.status)
        assertEquals(ValhallaLongUnits.miles, body.trip.units)
        assertEquals(1, body.trip.legs.count())
    }

    @Test
    fun testTraceAttributes() {
        // TODO: Add a regression test using the other encoded polyline (in testMapMatch).
        // It currently reveals a Valhalla bug, whereby we get back an edge_index in matched_points with a value of 2^64-1,
        // which is not representable in anything but a ULong, and the OpenAPI Generator doesn't support that.
        val req = TraceAttributesRequest(
            id = "map_match",
            encodedPolyline = "ge~jkAbakppC_AJ}_@hEsCZiCXuo@dH{l@pGuaBtPjBr`@`Bt]bDpu@FbDy@fEcAjEgOja@_BfE]zDoIzTaF~MwLtZsDjJwJ~ViF`N_DjI_AzBcDbIsJ~UqIvSqLvYoAzCwJdVmLjY_DzH_FzLuYls@aYhr@Uj@iCpGu@jBmLjYeJ~T_C|FoBzEkGrO}FtNaFxGkHpQeArHcGbOeEfK}D`K}Shi@uWxp@gMv[wBnFiAtCmMl[aB`EoKrW}Mj\\\\kDlI}K~WeBdEeHxPgF~LiBjE{BnF_EpJsD|I}Rte@sNp]Uj@aQ|b@w^||@_EtJi^`_AaC~F}Qnc@cHlOgA~B}DnIqG~M_NbXeO~X{O~X}\\\\nl@oFjJyRt\\\\iJ|NiGxJ{KbPeLtO_HhIyGjI_GbHc]v_@mNnOgWdYcFvFsGtFkBiEWg@cAfAqBnBnCnFaErDiTlSiWdV_O~N{QrOcFrE{IjIaGrC}G|IeEbGwDdGeDfGsDtHmDbIgI~SyEdMaBpEeB|E_BnEsLl\\\\qLj\\\\nxAfoA`Lr`ApB|Rz@`Lr@nLp@tJfCzW~Fhi@r@bGv@tF`AnFdA|ErA~EzAxE~BzF|I`SjAxC|@nCl@zB`@lBXtBPhBJ`BDfB@dDCjDGxDqIv}BUrIKpFE|C@|CFhD`@nM`@lJPnDJhAdkA|fMob@i`@gAZfEhgAT|FpDvEnKISjr@pHfe@dHjCvM{F{Lak@q@mZq[bbC~OnAvRvWcHrVzK`GzH}TxGdDoDrKyHlVwG`TvGaTfI|EnBxBZvBGj]AnDAlC\\\\lB~K~ZtoAlEuExOy@bFi@xHFxF^fEl@rDfBxGbC~GjEdKjQbm@oG|YvBpFrMbV~GlMlGtJtFlHlEjFnGtJzDjGzDvHjBtFrD`IzB`DzD~ClDjBzDjCzChAjD~@?vF^tHx@vGpA~DbB`CpBhArCSrDk@dEkAxB_AbC_BpBkB`@K`B]pFp@fDx@tDh@`BHrCJ~CKd@S`Bk@fAcA`AtP`UxCcAfUsChq@wBSkBSiB[}BiAwMbOvMcO|BhAhBZjBRvBRwC`o@g@vJjBeC~@c@tAz@xF`NnX~p@zAlDtAvA|Bv@xBQ??fo@vsBJhDpc@`aDhBrHzCtHzHtP}Vz_@iFjIiFpIoEbIaJnPzf@d_BuAlQiA~^qd@|dMcCru@G|Cq@|^]z_@LnV^x]RtHd@xP`Bdb@pCbStApIpAjFdCnHlCtFtBlDxDxEdBfBnCnC`~B`yMhA`A|bHvr`@~CxVbF_A~a@yJbS}EvDXdM|AvJ{F{gAjtDgCddC|f@jyDbEtPxuG|scBsDxMahGvkYlXhT`DvCfChC|BjCfBlC|C`GvDdI~_@jbAhJ`Vt|AltGoH~FcTdLqDlA}Af@zhAgl@jEzSnCnNnCjOfBnL~C`XfA~J|@`Kj@hJXtHzAtm@v@xSZrEh@pDdApDfDxFaDxDiA`DG~FpBz^v@tL~@hIt@jDhAdDpYxf@feAbhB{]va@iPuXaBeD`BdDhPtX{JjM~Jz]|@TvPaOjJ~OvNtV|N|Vla@xr@zWjd@fAjBkGlHuUnXqB`CaFgIoK_QmF`HuJcPiFfHo@~BUtBFtA`@zAdA~AfQ|Vb@p@|@rJcCrCxQ|YhmNn`hAk@zECnEbV`x@dBnBlGdAlCX|m@o[tBkDdAoFf@mCdMwGvE~NzGhLzDxE}FtIcC|Bcu@xq@eBzCe@hC~x@t~ElhZpalAln@mSAjKp@pJyKpBqSxHh|KdwX~eMnjv@B_BV_BXeAt@y@fLaChRwBdO]tKXpN~Cz[`N_A`CcArIjC|qAUjHmAnG}BzEyDxDyeAli@hoDxhMpHxEaBdMh@jN~I``@jLpk@v@~D~e@nqE|@`ChBlCRzAP`B|CvHl\\\\yNlT{HCnGLvBxFn[fBzJ~A|IzAnGpBrGrCvFnOtVfEdIhEvJhBpFiLpH{d@fZiAdE\\\\hF`tDtzJCFzt@lg@mCpIiDtKhDuKlCqIrD_MbNid@sY}Q{LiJkIqHHaI]wEX{H|AwG`HaNnBzBtLjK`MxIbNrI`e@xYxKjJpIrI~HrKzG`NzErKtDdL~C`OdCtOhFvj@mLdC[F|SxzB~LyBzAxPhChYl@`E`AbExAdDjBxC|BvBtBfBlCpAdDz@nDl@|D^pGf@lSzAxIn@kAxScA|QTvAf@~AdNtSrg@taCfAxGyHfF{KdHiAp@vc@rrBw@hOk@|K~bBh|W~CnHfF`GdMnGpH`AlIm@nh@kL~gAfrOCbMbAbMD`@~@pRMhLNrRaBbB?fLvG`@zC\\\\|I~@SrOI`FzAfGoKpJqKpJ}@pLjPf\\\\rFT`H}AhBjA|HXClFyAdHuD~Jm\\\\oDsUkAwM]eMxrBqLvcCoC|{@e@jk@rBrjD`Ar|CJz{C[l~B[|nA_ExnAgGds@wGxd@}G~f@mDvSkDpTsArIuNd~@cGxi@sHxcAy@bWcAfl@y@|m@aBhz@sAbp@[hNIbDqCncBuDn|CaA~pAEvJn@bnApAtaA`@dZfErr@xDrh@`Du@|EiEp@cEfBcF^|EsAzIaFbFcFnBdEbi@h@~GpFf@n@hA^rCnBb[qALkHj@NdKFhGkAvc@e@fFuB`LiAjG_Phw@q@fDwIrb@Ev@dCdJlBdCnC~@dFDwB~AbCdGfE`J}B~Km@lD?lArAdFcEdHoDzGyFrJpXlw@rCOpCIbA`@dAdA|CrJvBlBtB@~D{AjB]~A^dBdDeAdAuGdFzElOte@b{AhAlF~@hEdQzk@zGpV`BhK|@|HdArGlBhGmAlAiMdH`EfYjInObG|E~K|EfGrB`P~D_IxD_HdBeErBn@pn@KfKPpFl@xAbBjBnC~Vl@pFdG|b@iFbBzNjcApZjiBzd@vmCvL~s@tHiCdFh]pBnKpAlI`S|iAbBzJhIoDpL~r@jXuJvDvKqEzDkBp@pCpKyPlKkHbEyHdFlDtV~DjUbDvRsEpDeBoJeCoN[dQsAfMuDpImH`GiE`Dqf@zv@}a@vq@iDlMib@br@oRvYyIrNi[vg@cC|DqDvPoS~dCgLre@mJ|RkEjHkIpKuJ|HkOfJse@bXg^|Qy^~ZiH`JeC`DeNhYw@~AkGdS{Gt]mGtr@gGbr@wAlh@mBdgAa@vUqD|oA{K~zCcLfxAaBrX`NbEvMlRwMmRaNcEGnA_@dMCnB?hU@fJTfL^bJfFSbC_EcC~DfJa@gJ`@gFR\\\\|Q^zSR`KYz[Izc@CdJK|[C~Mg@~N_@fN}@bQ}Ktt@aBvKyiFy}JgP|Uqg@ds@mD|D_yAhaByB`GgB|HaAtF[dH?hFH|HjAxGxBxG`CvDnXp^`PaX~HcJrp@_p@",
            costing = MapMatchCostingModel.pedestrian,
            units = DistanceUnit.mi,
            language = ValhallaLanguages.enMinusGB
        )
        val res = service.traceAttributes(req).execute()
        val body = res.body() ?: fail("Request failed: ${res.errorBody()}")

        assertEquals(req.id, body.id)
        assertEquals(ValhallaLongUnits.miles, body.units)
        assertTrue(body.admins!!.isNotEmpty())
        assertTrue((body.edges?.count() ?: 0) > 1)
        assertTrue((body.matchedPoints?.count() ?: 0) > 1)
        assertTrue((body.shape?.count() ?: 0) > 1)
    }
}