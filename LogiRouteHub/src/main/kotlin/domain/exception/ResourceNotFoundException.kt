package domain.exception

class ResourceNotFoundException(
    resourceType: String,
    resourceId: String
) : DomainException(
    message = "$resourceType with ID $resourceId was not found"
)
