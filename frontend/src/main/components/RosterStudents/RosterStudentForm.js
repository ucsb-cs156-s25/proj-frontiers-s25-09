import React from "react";
import { useForm } from "react-hook-form";
import { useBackendMutation } from "main/utils/useBackend";
import { useNavigate, useParams } from "react-router-dom";

export default function RosterStudentForm({ initialContents, buttonLabel = "Create" }) {
  const { courseId, id } = useParams();
  const navigate = useNavigate();
  
  const { register, handleSubmit, formState: { errors } } = useForm({
    defaultValues: initialContents || {}
  });

  const objectToAxiosParams = (data) => ({
    url: id ? `/api/rosterstudents/post/${id}` : "/api/rosterstudents/post",
    method: id ? "PUT" : "POST",
    data: {
      ...data,
      courseId: courseId
    }
  });

  const onSuccess = () => {
    navigate(`/admin/courses/${courseId}/roster_students`);
  };

  const mutation = useBackendMutation(
    objectToAxiosParams,
    { onSuccess },
    ["/api/rosterstudents/course?courseId=${courseId}"]
  );

  return (
    <form onSubmit={handleSubmit(mutation.mutate)}>
      <div className="form-group">
        <label htmlFor="studentId">Student ID</label>
        <input
          type="text"
          className={`form-control ${errors.studentId ? "is-invalid" : ""}`}
          id="studentId"
          {...register("studentId", { required: "Student ID is required" })}
        />
        {errors.studentId && (
          <div className="invalid-feedback">{errors.studentId.message}</div>
        )}
      </div>

      {/* Repeat similar fields for firstName, lastName, email */}

      {mutation.error && (
        <div className="alert alert-danger">
          {mutation.error.response?.data?.error || "Error saving student"}
        </div>
      )}

      <button type="submit" className="btn btn-primary">
        {buttonLabel}
      </button>
    </form>
  );
}