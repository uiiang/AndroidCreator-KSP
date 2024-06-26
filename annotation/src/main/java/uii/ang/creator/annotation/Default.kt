package uii.ang.creator.annotation


@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Default(val valueAsString: String)

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class ParseRoot