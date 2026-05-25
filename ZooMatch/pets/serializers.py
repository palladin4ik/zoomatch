from rest_framework import serializers
from drf_spectacular.utils import extend_schema_field

from .models import (AnimalType, Breed, Tag, Pet, PetInfo, Comment)
from users.serializers import (UserSerializer, SimpleUserSerializer,
                               Base64FileField)

from moderation.models import ModerationRequest
from geo.services import build_location_from_input


class AnimalTypeSerializer(serializers.ModelSerializer):

    class Meta:
        model = AnimalType
        fields = ('id', 'name')


class BreedSerializer(serializers.ModelSerializer):
    animal_type = AnimalTypeSerializer(read_only=True)
    animal_type_id = serializers.PrimaryKeyRelatedField(
        queryset=AnimalType.objects.all(), write_only=True
    )

    class Meta:
        model = Breed
        fields = ('id', 'name', 'animal_type_id', 'animal_type')
        read_only_fields = ('id',)


class SimpleBreedSerializer(serializers.ModelSerializer):

    class Meta:
        model = Breed
        fields = ('id', 'name')
        read_only_fields = ('id', 'name')


class TagSerializer(serializers.ModelSerializer):

    class Meta:
        model = Tag
        fields = ('id', 'tag')
        read_only_fields = ('id',)


class PetSerializer(serializers.ModelSerializer):
    owner = SimpleUserSerializer(read_only=True)
    animal_type = AnimalTypeSerializer(read_only=True)
    breed = SimpleBreedSerializer(read_only=True)
    tags = serializers.SerializerMethodField()

    avatar = Base64FileField(read_only=True)
    pedigree_documents = serializers.CharField(read_only=True)

    class Meta:
        model = Pet
        fields = ('id', 'name', 'animal_type', 'breed', 'is_male',
                  'age', 'owner', 'avatar', 'location', 'has_pedigree',
                  'pedigree_documents', 'awards', 'tags', 'description',
                  'is_active')
        read_only_fields = ('id',)

    @extend_schema_field(TagSerializer(many=True))
    def get_tags(self, obj):
        return TagSerializer(obj.tags.all(), many=True).data


class PetCreateUpdateSerializer(serializers.ModelSerializer):
    tags = serializers.ListField(
        child=serializers.CharField(max_length=50),
        required=False,
        write_only=True
    )
    tags_list = serializers.SerializerMethodField(read_only=True)

    avatar = Base64FileField(required=False, allow_null=True, allow_blank=True)
    pedigree_documents = Base64FileField(required=False, allow_null=True,
                                         allow_blank=True)

    animal_type_custom = serializers.CharField(required=False,
                                               allow_blank=True)
    breed_custom = serializers.CharField(required=False, allow_blank=True)

    address = serializers.CharField(write_only=True, required=False)
    latitude = serializers.FloatField(write_only=True, required=False)
    longitude = serializers.FloatField(write_only=True, required=False)

    class Meta:
        model = Pet
        fields = ('id', 'name', 'animal_type', 'breed', 'is_male',
                  'age', 'avatar', 'location', 'address', 'latitude',
                  'longitude', 'has_pedigree', 'pedigree_documents',
                  'awards', 'tags', 'tags_list', 'description',
                  'is_active', 'animal_type_custom', 'breed_custom')
        read_only_fields = ('id', 'location')

    @extend_schema_field(TagSerializer(many=True))
    def get_tags_list(self, obj):
        return TagSerializer(obj.tags.all(), many=True).data

    def validate(self, data):
        breed_custom = data.get('breed_custom')
        animal_type = data.get('animal_type')
        animal_type_custom = data.get('animal_type_custom')

        if breed_custom and not animal_type and not animal_type_custom:
            raise serializers.ValidationError(
                'Укажите тип животного для новой породы'
            )
        return data

    def validate_tags(self, values):
        cleaned = []
        for value in values:
            tag_name = value.strip().lower()
            if tag_name not in cleaned:
                cleaned.append(tag_name)
        return cleaned

    def create(self, validated_data):
        animal_type_custom = validated_data.pop('animal_type_custom', None)
        breed_custom = validated_data.pop('breed_custom', None)

        tags_data = validated_data.pop('tags', [])

        if animal_type_custom or breed_custom:
            validated_data['is_active'] = False

        breed = validated_data.get('breed')
        if breed:
            validated_data['animal_type'] = breed.animal_type

        address = validated_data.pop('address', None)
        latitude = validated_data.pop('latitude', None)
        longitude = validated_data.pop('longitude', None)

        validated_data['location'] = build_location_from_input(
            address, latitude, longitude
        )

        pet = Pet.objects.create(**validated_data)

        if animal_type_custom or breed_custom:
            ModerationRequest.objects.create(
                pet=pet,
                animal_type=animal_type_custom,
                breed=breed_custom
            )

        for tag_name in tags_data:
            tag, _ = Tag.objects.get_or_create(tag=tag_name)
            pet.tags.add(tag)

        return pet

    def update(self, instance, validated_data):
        tags_data = validated_data.pop('tags', None)

        address = validated_data.pop('address', None)
        latitude = validated_data.pop('latitude', None)
        longitude = validated_data.pop('longitude', None)

        if (address or latitude is not None or longitude is not None):
            validated_data['location'] = build_location_from_input(
                address, latitude, longitude
            )

        breed = validated_data.get('breed')
        if breed:
            validated_data['animal_type'] = breed.animal_type

        for attr, value in validated_data.items():
            setattr(instance, attr, value)

        instance.save()

        if tags_data is not None:
            instance.tags.clear()
            for tag_name in tags_data:
                tag, _ = Tag.objects.get_or_create(tag=tag_name)
                instance.tags.add(tag)

        return instance


class PetInfoSerializer(serializers.ModelSerializer):
    pet_id = serializers.PrimaryKeyRelatedField(
        queryset=Pet.objects.all(), source='pet'
    )

    class Meta:
        model = PetInfo
        fields = ('id', 'pet_id', 'watchers', 'likes')
        read_only_fields = ('id', 'pet_id', 'watchers', 'likes')


class CommentSerializer(serializers.ModelSerializer):
    author = UserSerializer()
    pet_info_card_id = serializers.PrimaryKeyRelatedField(
        queryset=PetInfo.objects.all(), source='pet_info_card'
    )

    class Meta:
        model = Comment
        fields = ('id', 'content', 'media', 'likes', 'author',
                  'pet_info_card_id', 'date_create')
        read_only_fields = ('id', 'likes', 'author', 'date_create')

    def create(self, validated_data):
        validated_data['author'] = self.context['request'].user
        return super().create(validated_data)
