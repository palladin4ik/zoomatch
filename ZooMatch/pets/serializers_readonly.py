from rest_framework import serializers
from django.contrib.auth import get_user_model

from .models import AnimalType, Breed, Pet

User = get_user_model()


class PetShortOwnerSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('id', 'firstname', 'lastname', 'avatar')


class AnimalTypeIdNameSerializer(serializers.ModelSerializer):
    class Meta:
        model = AnimalType
        fields = ('id', 'name')


class BreedIdNameSerializer(serializers.ModelSerializer):
    class Meta:
        model = Breed
        fields = ('id', 'name')


class PetShortSerializer(serializers.ModelSerializer):
    distance_km = serializers.SerializerMethodField()
    animal_type = AnimalTypeIdNameSerializer(read_only=True)
    breed = BreedIdNameSerializer(read_only=True)
    animal_type_custom = serializers.SerializerMethodField()
    breed_custom = serializers.SerializerMethodField()
    moderation_status = serializers.SerializerMethodField()
    owner = PetShortOwnerSerializer(read_only=True)

    class Meta:
        model = Pet
        fields = ('id', 'name', 'is_male', 'age', 'avatar', 'is_active',
                  'animal_type', 'breed', 'distance_km', 'location',
                  'animal_type_custom', 'breed_custom', 'moderation_status',
                  'has_pedigree', 'owner')

    def get_distance_km(self, obj) -> float | None:
        distance = getattr(obj, 'distance_km', None)

        if distance is None:
            return None

        return round(distance, 2)

    def get_animal_type_custom(self, obj) -> str | None:
        mod = getattr(obj, 'moderation_request', None)
        if mod and mod.animal_type:
            return mod.animal_type
        return None

    def get_breed_custom(self, obj) -> str | None:
        mod = getattr(obj, 'moderation_request', None)
        if mod and mod.breed:
            return mod.breed
        return None

    def get_moderation_status(self, obj) -> str | None:
        mod = getattr(obj, 'moderation_request', None)
        if mod is None:
            return None
        from moderation.models import ModerationRequest
        status_map = {
            ModerationRequest.Status.PENDING: 'pending',
            ModerationRequest.Status.APPROVED: 'approved',
            ModerationRequest.Status.REJECTED: 'rejected',
        }
        return status_map.get(mod.status, None)
