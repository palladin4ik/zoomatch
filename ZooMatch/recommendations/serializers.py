from rest_framework import serializers


class RecommendationParamsSerializer(serializers.Serializer):
    pet_id = serializers.IntegerField()
    radius_km = serializers.ChoiceField(
        choices=[50, 150, 300],
        required=False,
        allow_null=True,
        default=None,
    )
    requires_pedigree = serializers.BooleanField(required=False, default=False)
    min_age = serializers.IntegerField(required=False, allow_null=True,
                                       default=None)
    max_age = serializers.IntegerField(required=False, allow_null=True,
                                       default=None)
    max_months_since_mating = serializers.IntegerField(
        required=False, allow_null=True, default=None)
