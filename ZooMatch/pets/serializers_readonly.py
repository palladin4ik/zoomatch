from rest_framework import serializers

from .models import Pet


class PetShortSerializer(serializers.ModelSerializer):
    distance_km = serializers.SerializerMethodField()

    class Meta:
        model = Pet
        fields = ('id', 'name', 'is_male', 'age', 'avatar', 'is_active',
                  'animal_type', 'breed', 'distance_km')

    def get_distance_km(self, obj) -> float | None:
        distance = getattr(obj, 'distance_km', None)

        if distance is None:
            return None

        return round(distance, 2)
