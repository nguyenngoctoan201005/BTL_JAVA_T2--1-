package com.coffeeshop.backend.controller;

import com.coffeeshop.backend.dto.shift.CloseShiftRequest;
import com.coffeeshop.backend.dto.shift.OpenShiftRequest;
import com.coffeeshop.backend.dto.shift.ShiftResponse;
import com.coffeeshop.backend.service.ShiftService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/v1/shifts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ShiftController {

    @Inject
    ShiftService shiftService;

    @GET
    public Response getAllShifts() {
        List<ShiftResponse> shifts = shiftService.getAllShifts();
        return Response.ok(shifts).build();
    }

    @POST
    @Path("/open")
    public Response openShift(OpenShiftRequest request) {
        shiftService.openShift(request);
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/{id}/close")
    public Response closeShift(@PathParam("id") Long id, CloseShiftRequest request) {
        shiftService.closeShift(id, request);
        return Response.ok().build();
    }
}
